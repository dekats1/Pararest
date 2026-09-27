package com.actisys.compositeservice.client.impl;

import com.actisys.compositeservice.client.MenuServiceClient;
import com.actisys.compositeservice.config.ServiceProperties.MenuServiceProperties;
import com.actisys.compositeservice.dto.menu.AddMenuItem;
import com.actisys.compositeservice.dto.menu.AddMenuItemDTO;
import com.actisys.compositeservice.dto.order.MenuItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class MenuServiceClientImpl implements MenuServiceClient {

    private final WebClient webClient;
    private final MenuServiceProperties menuProperties;

    public MenuServiceClientImpl(WebClient.Builder webClientBuilder, MenuServiceProperties menuProperties) {
        this.menuProperties = menuProperties;
        this.webClient = webClientBuilder
                .baseUrl(menuProperties.getHost())
                .build();
    }

    @Override
    public Mono<Long> createDish(AddMenuItemDTO dishDto, String username) {
        String url = menuProperties.getHost() + menuProperties.getEndpoints().getAddNew();

        log.debug("Creating dish in menu service: {}", url);
        log.debug("Username header: {}", username);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Username", username)
                .bodyValue(dishDto)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Menu service error response: {}", errorBody);
                                    return Mono.error(new RuntimeException("Menu service error: " + errorBody));
                                })
                )
                .bodyToMono(Long.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof TimeoutException ||
                                throwable instanceof IOException))
                .doOnSuccess(success -> {
                    if (success != null) {
                        log.info("Dish created successfully with id: {}", success);
                    } else {
                        log.warn("Dish creation returned null");
                    }
                })
                .doOnError(e -> log.error("Failed to create dish: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deleteDish(Long dishId, String username) {
        String url = menuProperties.getHost() + "/api/v1/menu/deleteMenuItem/" + dishId;

        log.debug("Deleting dish in menu service: {}", url);
        log.debug("Username for deletion: {}", username);

        return webClient.delete()
                .uri(url)
                .header("X-Username", username)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Dish {} deleted successfully", dishId))
                .doOnError(e -> log.error("Failed to delete dish {}: {}", dishId, e.getMessage()));
    }

    @Override
    public Mono<List<MenuItemDTO>> getDishesByIds(List<Long> ids) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(menuProperties.getEndpoints().getGetDishesByIds())
                        .queryParam("menuIds", ids)
                        .build())
                .retrieve()
                .bodyToFlux(MenuItemDTO.class)
                .collectList()
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(dish -> log.info("Dish {} retrieved successfully", ids))
                .doOnError(e -> log.error("Failed to get dish {}: {}", ids, e.getMessage()));
    }
}