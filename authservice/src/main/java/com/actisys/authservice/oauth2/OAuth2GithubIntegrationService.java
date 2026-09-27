package com.actisys.authservice.oauth2;

import com.actisys.authservice.client.UserServiceClient;
import com.actisys.authservice.model.AuthProvider;
import com.actisys.authservice.model.Credential;
import com.actisys.authservice.repository.AuthRepository;
import com.actisys.authservice.util.JwtTokenProvider;
import com.actisys.dto.CreateUserRequest;
import com.actisys.dto.UserAddDTO;
import com.actisys.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2GithubIntegrationService {

    private final AuthRepository authRepository;
    private final UserServiceClient userServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Runs in a transactional boundary when invoked from reactive OAuth flows (boundedElastic).
     */
    @Transactional
    public void ensureGithubAccountLinked(GitHubOAuth2User githubUser) {
        String providerId = githubUser.getProviderId();
        String githubLogin = githubUser.getGithubLogin();
        String email = githubUser.getEmail();
        String displayName = githubUser.getDisplayName();

        log.info("Processing GitHub OAuth2 user: login={}, providerId={}", githubLogin, providerId);

        Optional<Credential> existing = authRepository
                .findByProviderIdAndAuthProvider(providerId, AuthProvider.GITHUB);

        if (existing.isPresent()) {
            log.info("GitHub user already registered: {}", githubLogin);
            return;
        }

        if (email != null) {
            Optional<Credential> byEmail = authRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                Credential cred = byEmail.get();
                cred.setAuthProvider(AuthProvider.GITHUB);
                cred.setProviderId(providerId);
                authRepository.save(cred);
                log.info("Linked GitHub provider to existing account with email={}", email);
                return;
            }
        }

        UUID userId = UUID.randomUUID();
        String uniqueLogin = resolveUniqueLogin(githubLogin);
        String resolvedEmail = (email != null) ? email : uniqueLogin + "@github.noemail";

        Credential credential = Credential.builder()
                .id(userId)
                .login(uniqueLogin)
                .email(resolvedEmail)
                .password(null)
                .authProvider(AuthProvider.GITHUB)
                .providerId(providerId)
                .build();

        authRepository.save(credential);

        UserAddDTO userDto = UserAddDTO.builder()
                .login(uniqueLogin)
                .email(resolvedEmail)
                .fullName(displayName)
                .build();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .id(userId)
                .user(userDto)
                .build();

        try {
            userServiceClient.createUser(createUserRequest).block();
            log.info("Created new user from GitHub OAuth2: login={}, userId={}", uniqueLogin, userId);
        } catch (Exception e) {
            log.error("Failed to create user profile in user-service, rolling back credential", e);
            authRepository.delete(credential);
            throw new UserServiceException("Failed to create user profile: " + e.getMessage());
        }
    }

    @Transactional
    public OAuthTokens finalizeOAuthLogin(GitHubOAuth2User githubUser) {
        Credential credential = authRepository
                .findByProviderIdAndAuthProvider(githubUser.getProviderId(), AuthProvider.GITHUB)
                .orElseGet(() -> authRepository.findByEmail(githubUser.getEmail())
                        .orElseThrow(() -> new IllegalStateException(
                                "Credential not found for GitHub user: " + githubUser.getProviderId())));

        String accessToken = jwtTokenProvider.generate(credential.getId(), credential.getLogin());
        String refreshToken = jwtTokenProvider.generateRefreshToken(credential.getId(), credential.getLogin());

        credential.setRefreshToken(refreshToken);
        authRepository.save(credential);

        return new OAuthTokens(accessToken, refreshToken);
    }

    private String resolveUniqueLogin(String githubLogin) {
        if (!authRepository.existsByLogin(githubLogin)) {
            return githubLogin;
        }
        String candidate = githubLogin + "_gh";
        int i = 2;
        while (authRepository.existsByLogin(candidate)) {
            candidate = githubLogin + "_gh" + i++;
        }
        return candidate;
    }
}
