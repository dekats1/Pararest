package com.actisys.analyticsservice.client.impl;

import com.actisys.analyticsservice.client.PaymentServiceClient;
import com.actisys.analyticsservice.configuration.properties.PaymentServiceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class PaymentServiceClientImpl implements PaymentServiceClient {

    private final WebClient webClient;
    private final PaymentServiceProperties properties;

    public PaymentServiceClientImpl(WebClient.Builder webClientBuilder,
                                    PaymentServiceProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getHost())
                .build();
    }

    @Override
    public Mono<BigDecimal> getTotalAmount() {
        String url = properties.getEndpoints().getTotalAmount();
        log.debug("Fetching total revenue from payment service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Payment service error: {}", body);
                                    return Mono.error(new RuntimeException("Payment service error: " + body));
                                })
                )
                .bodyToMono(BigDecimal.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException))
                .doOnSuccess(v -> log.info("Total revenue fetched: {}", v))
                .doOnError(e -> log.error("Failed to fetch total revenue: {}", e.getMessage()));
    }

    @Override
    public Mono<BigDecimal> getRevenueAmount() {
        String url = properties.getEndpoints().getRevenueAmount();
        log.debug("Fetching filtered revenue from payment service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Payment service error: " + body)))
                )
                .bodyToMono(BigDecimal.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException));
    }

    @Override
    public Mono<Long> getRevenueOperationsCount() {
        String url = properties.getEndpoints().getRevenueOperationsCount();
        log.debug("Fetching revenue operations count from payment service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Payment service error: " + body)))
                )
                .bodyToMono(Long.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException));
    }
}