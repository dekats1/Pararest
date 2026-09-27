package com.actisys.authservice.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ReactiveAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final OAuth2GithubIntegrationService githubIntegrationService;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        GitHubOAuth2User githubUser = new GitHubOAuth2User(oAuth2User);

        return Mono.fromCallable(() -> githubIntegrationService.finalizeOAuthLogin(githubUser))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tokens -> {
                    log.info("OAuth2 success for user={}, redirecting to frontend", githubUser.getGithubLogin());

                    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                            .queryParam("token", tokens.accessToken())
                            .queryParam("refreshToken", tokens.refreshToken())
                            .build()
                            .toUriString();

                    ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
                    response.setStatusCode(HttpStatus.FOUND);
                    response.getHeaders().setLocation(URI.create(targetUrl));
                    return response.setComplete();
                });
    }
}
