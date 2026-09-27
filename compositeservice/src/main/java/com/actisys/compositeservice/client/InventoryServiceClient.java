package com.actisys.compositeservice.client;

import com.actisys.dto.AddRecipeDTO;
import reactor.core.publisher.Mono;

public interface InventoryServiceClient {
    Mono<Boolean> createRecipe(AddRecipeDTO recipeDTO);
    Mono<Void> deleteRecipeByDishId(Long dishId);
    Mono<AddRecipeDTO> getRecipeByDishId(Long dishId);
}