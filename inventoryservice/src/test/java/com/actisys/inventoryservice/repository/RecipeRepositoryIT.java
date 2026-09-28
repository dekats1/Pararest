package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Metrics;
import com.actisys.inventoryservice.model.Recipe;
import com.actisys.inventoryservice.model.RecipeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RecipeRepositoryIT {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @DisplayName("findByIdMenuItemId() возвращает ингредиенты рецепта по id блюда")
    void findByIdMenuItemId_returnsRecipeItems() {
        Ingredient ingredient = ingredientRepository.save(Ingredient.builder()
                .name("Cheese").unit(Metrics.KG)
                .currentStock(BigDecimal.TEN).minStock(BigDecimal.ONE).build());

        Recipe recipe = Recipe.builder()
                .id(new RecipeId(10L, ingredient.getId()))
                .ingredient(ingredient)
                .quantity(2)
                .build();
        recipeRepository.save(recipe);

        List<Recipe> result = recipeRepository.findByIdMenuItemId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
    }
}
