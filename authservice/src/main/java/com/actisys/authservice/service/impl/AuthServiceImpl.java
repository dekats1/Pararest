package com.actisys.authservice.service.impl;

import com.actisys.authservice.client.UserServiceClient;
import com.actisys.authservice.dto.JwtResponse;
import com.actisys.authservice.dto.LoginRequest;
import com.actisys.authservice.dto.RegisterRequest;
import com.actisys.authservice.exception.EmailAlreadyExistsException;
import com.actisys.authservice.exception.InvalidCredentialsException;
import com.actisys.authservice.exception.UserAlreadyExistsException;
import com.actisys.authservice.exception.UserNotFoundException;
import com.actisys.authservice.model.AuthProvider;
import com.actisys.authservice.model.Credential;
import com.actisys.authservice.repository.AuthRepository;
import com.actisys.authservice.service.AuthService;
import com.actisys.authservice.util.JwtTokenProvider;
import com.actisys.dto.UserAddDTO;
import com.actisys.dto.CreateUserRequest;
import com.actisys.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JwtResponse register(RegisterRequest request) {
        if (authRepository.existsByLogin(request.getNewUser().getLogin())) {
            throw new UserAlreadyExistsException("User with login '" + request.getNewUser().getLogin()
                    + "' already exists");
        }
        if (authRepository.existsByEmail(request.getNewUser().getEmail())) {
            throw new EmailAlreadyExistsException("User with email '" + request.getNewUser().getEmail()
                    + "' already exists");
        }

        UUID userId = UUID.randomUUID();
        String login = request.getNewUser().getLogin();
        String accessToken = jwtTokenProvider.generate(userId, login);
        String refreshToken = null;

        if (request.isRememberMe()) {
            refreshToken = jwtTokenProvider.generateRefreshToken(userId, login);
        }

        Credential credential = Credential.builder()
                .id(userId)
                .login(login)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getNewUser().getEmail())
                .refreshToken(refreshToken)
                .authProvider(AuthProvider.LOCAL)
                .build();

        authRepository.save(credential);

        UserAddDTO userForSave = UserAddDTO.builder()
                .login(login)
                .email(request.getNewUser().getEmail())
                .fullName(request.getNewUser().getFullName())
                .phone(request.getNewUser().getPhone())
                .birthday(request.getNewUser().getBirthday())
        .build();
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .id(userId)
                .user(userForSave)
                .build();
        try{
            log.info("Sending to userservice: {}", userRequest.getId());
            log.info("User in request: {}", userRequest.getUser().getLogin());
            userServiceClient.createUser(userRequest).block(Duration.ofSeconds(15));
        } catch (UserServiceException e){
            log.error(e.getMessage());
            authRepository.delete(credential);
            throw new UserServiceException(e.getMessage());
        }

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        Credential credential = authRepository.findByLogin(request.getLogin()).orElseThrow(
                ()->new UserNotFoundException("No users with this credentials"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generate(credential.getId(), credential.getLogin());
        String refreshToken = null;

        if (request.getRememberMe()) {
            refreshToken = jwtTokenProvider.generateRefreshToken(credential.getId(), credential.getLogin());

            credential.setRefreshToken(refreshToken);
            authRepository.save(credential);
        }

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    public JwtResponse refreshToken(UUID id) {
        Credential credential = authRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No credentials for this user"));
        credential.setRefreshToken(jwtTokenProvider.generateRefreshToken(credential.getId(), credential.getLogin()));
        return new JwtResponse(jwtTokenProvider.generate(credential.getId(), credential.getLogin()),
                jwtTokenProvider.generateRefreshToken(credential.getId(), credential.getLogin()));
    }
}