package com.actisys.compositeservice.client.impl;

import com.actisys.compositeservice.client.InventoryServiceClient;
import com.actisys.compositeservice.config.ServiceProperties.InventoryServiceProperties;
import com.actisys.dto.AddRecipeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class InventoryServiceClientImpl implements InventoryServiceClient {

    private final WebClient webClient;
    private final InventoryServiceProperties inventoryProperties;

    public InventoryServiceClientImpl(WebClient.Builder webClientBuilder, InventoryServiceProperties inventoryProperties) {
        this.inventoryProperties = inventoryProperties;
        this.webClient = webClientBuilder
                .baseUrl(inventoryProperties.getHost())
                .build();
    }

    @Override
    public Mono<Boolean> createRecipe(AddRecipeDTO recipeDTO) {
        String url = inventoryProperties.getHost() + inventoryProperties.getEndpoints().getAddNewRecipe();

        log.debug("Creating recipe in inventory service for dish ID: {}", recipeDTO.getMenuItemId());

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(recipeDTO)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .doOnSuccess(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        log.info("Recipe created successfully for dish ID: {}", recipeDTO.getMenuItemId());
                    } else {
                        log.warn("Recipe creation returned false for dish ID: {}", recipeDTO.getMenuItemId());
                    }
                })
                .doOnError(e -> log.error("Failed to create recipe for dish {}: {}",
                        recipeDTO.getMenuItemId(), e.getMessage()));
    }

    @Override
    public Mono<Void> deleteRecipeByDishId(Long dishId) {
        String url = inventoryProperties.getHost() + inventoryProperties.getEndpoints().getDeleteRecipeById() + dishId;

        log.debug("Deleting recipe for dish ID: {} from inventory service", dishId);

        return webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(v -> log.info("Recipe for dish {} deleted successfully", dishId))
                .onErrorResume(e -> {
                    log.error("Failed to delete recipe for dish {}: {}", dishId, e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<AddRecipeDTO> getRecipeByDishId(Long dishId) {
        String url = inventoryProperties.getHost() + inventoryProperties.getEndpoints().getGetRecipeByDishId() + dishId;

        log.debug("Getting recipe for dish ID: {} from inventory service", dishId);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(AddRecipeDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(recipe -> log.info("Recipe for dish {} retrieved successfully", dishId))
                .doOnError(e -> log.error("Failed to get recipe for dish {}: {}", dishId, e.getMessage()));
    }
}