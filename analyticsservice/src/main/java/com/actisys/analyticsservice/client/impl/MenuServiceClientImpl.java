package com.actisys.analyticsservice.client.impl;

import com.actisys.analyticsservice.client.MenuServiceClient;
import com.actisys.analyticsservice.configuration.properties.MenuServiceProperties;
import com.actisys.analyticsservice.dto.MenuItemAnalyticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class MenuServiceClientImpl implements MenuServiceClient {

    private final WebClient webClient;
    private final MenuServiceProperties properties;

    public MenuServiceClientImpl(WebClient.Builder webClientBuilder,
                                 MenuServiceProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getHost())
                .build();
    }

    @Override
    public Mono<List<MenuItemAnalyticsDTO>> getAllMenuItems() {
        String url = properties.getEndpoints().getGetAll();
        log.debug("Fetching menu items from menu service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Menu service error: {}", body);
                                    return Mono.error(new RuntimeException("Menu service error: " + body));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<List<MenuItemAnalyticsDTO>>() {})
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException))
                .doOnSuccess(v -> log.info("Menu items fetched: {}", v.size()))
                .doOnError(e -> log.error("Failed to fetch menu items: {}", e.getMessage()));
    }
}
