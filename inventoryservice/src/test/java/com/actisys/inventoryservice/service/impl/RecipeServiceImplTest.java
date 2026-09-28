package com.actisys.inventoryservice.service.impl;

import com.actisys.dto.AddRecipeDTO;
import com.actisys.inventoryservice.dto.RecipeDTO;
import com.actisys.inventoryservice.exception.RecipeNotFoundException;
import com.actisys.inventoryservice.mapper.RecipeMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Recipe;
import com.actisys.inventoryservice.model.RecipeId;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredient = Ingredient.builder().id(1L).name("Cheese").build();
    }

    @Test
    @DisplayName("addNewRecipe() сохраняет все ингредиенты рецепта")
    void addNewRecipe_savesAllItems() {
        HashMap<String, Integer> ingredientsMap = new LinkedHashMap<>();
        ingredientsMap.put("1", 2);

        AddRecipeDTO addRecipeDTO = AddRecipeDTO.builder()
                .menuItemId(10L)
                .ingredientsMap(ingredientsMap)
                .build();

        given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));
        given(recipeRepository.save(any(Recipe.class))).willAnswer(inv -> inv.getArgument(0));

        Boolean result = recipeService.addNewRecipe(addRecipeDTO);

        assertThat(result).isTrue();
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    @DisplayName("addNewRecipe() выбрасывает исключение при отсутствии ингредиента")
    void addNewRecipe_ingredientNotFound_throwsException() {
        HashMap<String, Integer> ingredientsMap = new LinkedHashMap<>();
        ingredientsMap.put("99", 1);

        AddRecipeDTO addRecipeDTO = AddRecipeDTO.builder()
                .menuItemId(10L)
                .ingredientsMap(ingredientsMap)
                .build();

        given(ingredientRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.addNewRecipe(addRecipeDTO))
                .isInstanceOf(RecipeNotFoundException.class);

        verify(recipeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getRecipeByMenuId() возвращает список ингредиентов рецепта")
    void getRecipeByMenuId_returnsRecipes() {
        Recipe recipe = Recipe.builder()
                .id(new RecipeId(10L, 1L))
                .ingredient(ingredient)
                .quantity(2)
                .build();
        RecipeDTO recipeDTO = RecipeDTO.builder()
                .id(new RecipeId(10L, 1L))
                .quantity(2)
                .build();

        given(recipeRepository.findByIdMenuItemId(10L)).willReturn(List.of(recipe));
        given(recipeMapper.toDTO(recipe)).willReturn(recipeDTO);

        List<RecipeDTO> result = recipeService.getRecipeByMenuId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
    }
}
