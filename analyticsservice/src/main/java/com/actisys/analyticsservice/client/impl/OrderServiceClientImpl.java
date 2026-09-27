package com.actisys.analyticsservice.client.impl;

import com.actisys.analyticsservice.client.OrderServiceClient;
import com.actisys.analyticsservice.configuration.properties.OrderServiceProperties;
import com.actisys.analyticsservice.dto.OrderAnalyticsDTO;
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
public class OrderServiceClientImpl implements OrderServiceClient {

    private final WebClient webClient;
    private final OrderServiceProperties properties;

    public OrderServiceClientImpl(WebClient.Builder webClientBuilder,
                                  OrderServiceProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getHost())
                .build();
    }

    @Override
    public Mono<Long> getTotalCount() {
        String url = properties.getEndpoints().getTotalCount();
        log.debug("Fetching total orders count from order service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Order service error: {}", body);
                                    return Mono.error(new RuntimeException("Order service error: " + body));
                                })
                )
                .bodyToMono(Long.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException))
                .doOnSuccess(v -> log.info("Total orders count fetched: {}", v))
                .doOnError(e -> log.error("Failed to fetch total orders count: {}", e.getMessage()));
    }

    @Override
    public Mono<List<OrderAnalyticsDTO>> getAllOrders() {
        String url = properties.getEndpoints().getAllOrders();
        log.debug("Fetching all orders from order service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Order service error: {}", body);
                                    return Mono.error(new RuntimeException("Order service error: " + body));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<List<OrderAnalyticsDTO>>() {})
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException))
                .doOnSuccess(v -> log.info("Orders fetched: {}", v.size()))
                .doOnError(e -> log.error("Failed to fetch orders: {}", e.getMessage()));
    }
}