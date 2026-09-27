package com.actisys.authservice.client;


import com.actisys.dto.CreateUserRequest;
import com.actisys.exception.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient webClient;

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.user-service.create-user.url}")
    private String createUserUrl;

    public Mono<Void> createUser(CreateUserRequest user) {
        return webClient.post()
                .uri(userServiceUrl + createUserUrl)
                .bodyValue(user)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new UserServiceException("UserService error: " + errorBody)))
                )
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(v -> log.info("User profile created successfully for userId: {}", user.getId()))
                .doOnError(e -> log.error("Failed to call UserService for userId: {}", user.getId(), e))
                .onErrorMap(WebClientResponseException.class, e ->
                        new UserServiceException("Failed to create user in UserService: " + e.getMessage(), e))
                .onErrorMap(Exception.class, e ->
                        e instanceof UserServiceException ? e : new UserServiceException("UserService is unavailable: " + e.getMessage(), e));
    }
}