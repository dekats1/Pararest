package com.actisys.compositeservice.service.impl;

import com.actisys.compositeservice.client.InventoryServiceClient;
import com.actisys.compositeservice.client.MenuServiceClient;
import com.actisys.compositeservice.dto.menu.AddMenuItem;
import com.actisys.compositeservice.service.MenuService;
import com.actisys.dto.AddRecipeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuServiceClient menuServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    @Override
    public Mono<ResponseEntity<Boolean>> addNewDish(AddMenuItem menuItem, String username) {
        String transactionId = UUID.randomUUID().toString();
        log.info("Starting transaction {} for new dish with username: {}", transactionId, username);

        return menuServiceClient.createDish(menuItem.getItem(), username)
                .flatMap(dishId -> {
                    log.info("Transaction {}: Dish created with ID: {}", transactionId, dishId);

                    AddRecipeDTO recipeDTO = AddRecipeDTO.builder()
                            .menuItemId(dishId)
                            .ingredientsMap(menuItem.getIngredientsMap())
                            .build();
                    return inventoryServiceClient.createRecipe(recipeDTO)
                            .flatMap(recipeCreated -> {
                                if (Boolean.TRUE.equals(recipeCreated)) {
                                    log.info("Transaction {}: Recipe created successfully for dish {}",
                                            transactionId, dishId);
                                    return Mono.just(ResponseEntity.ok(true));
                                } else {
                                    log.warn("Transaction {}: Recipe creation failed for dish {}",
                                            transactionId, dishId);
                                    return rollbackDish(dishId, transactionId, username)
                                            .thenReturn(ResponseEntity.badRequest().body(false));
                                }
                            })
                            .onErrorResume(error -> {
                                log.error("Transaction {}: Error creating recipe for dish {}",
                                        transactionId, dishId, error);
                                return rollbackDish(dishId, transactionId, username)
                                        .thenReturn(ResponseEntity.badRequest().body(false));
                            });
                })
                .onErrorResume(error -> {
                    log.error("Transaction {}: Failed to create dish", transactionId, error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(false));
                });
    }

    private Mono<Void> rollbackDish(Long dishId, String transactionId, String username) {
        log.info("Transaction {}: Rolling back dish {}", transactionId, dishId);
        return menuServiceClient.deleteDish(dishId, username)
                .doOnSuccess(v -> log.info("Transaction {}: Dish {} rolled back", transactionId, dishId))
                .onErrorResume(error -> {
                    log.error("Transaction {}: Failed to rollback dish {}", transactionId, dishId, error);
                    return Mono.empty();
                });
    }
}