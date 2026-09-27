package com.actisys.analyticsservice.client.impl;

import com.actisys.analyticsservice.client.InventoryServiceClient;
import com.actisys.analyticsservice.configuration.properties.InventoryServiceProperties;
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
public class InventoryServiceClientImpl implements InventoryServiceClient {

    private final WebClient webClient;
    private final InventoryServiceProperties properties;

    public InventoryServiceClientImpl(WebClient.Builder webClientBuilder,
                                      InventoryServiceProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getHost())
                .build();
    }

    @Override
    public Mono<BigDecimal> getTotalSuppliesAmount() {
        String url = properties.getEndpoints().getTotalSuppliesAmount();
        log.debug("Fetching total supplies amount from inventory service: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Inventory service error: {}", body);
                                    return Mono.error(new RuntimeException("Inventory service error: " + body));
                                })
                )
                .bodyToMono(BigDecimal.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof TimeoutException || t instanceof IOException))
                .doOnSuccess(v -> log.info("Total supplies amount fetched: {}", v))
                .doOnError(e -> log.error("Failed to fetch total supplies amount: {}", e.getMessage()));
    }
}