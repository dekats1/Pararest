package com.actisys.inventoryservice.service.impl;

import com.actisys.dto.AddRecipeDTO;
import com.actisys.inventoryservice.dto.RecipeDTO;
import com.actisys.inventoryservice.exception.RecipeNotFoundException;
import com.actisys.inventoryservice.mapper.RecipeMapper;
import com.actisys.inventoryservice.model.Recipe;
import com.actisys.inventoryservice.model.RecipeId;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.repository.RecipeRepository;
import com.actisys.inventoryservice.service.RecipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;

    @Override
    @Transactional
    public Boolean addNewRecipe(AddRecipeDTO addRecipeDTO) {
        for(Map.Entry<String, Integer>  item: addRecipeDTO.getIngredientsMap().entrySet()) {
            Recipe recipe = Recipe.builder()
                    .id(new RecipeId(addRecipeDTO.getMenuItemId(), Long.valueOf(item.getKey())))
                    .ingredient(ingredientRepository.findById(Long.valueOf(item.getKey())).orElseThrow(()->new RecipeNotFoundException(Long.valueOf(item.getKey()))))
                    .quantity(item.getValue())
                    .build();
            recipeRepository.save(recipe);
        }
        return true;
    }

    @Override
    public List<RecipeDTO> getRecipeByMenuId(Long id) {
        List<Recipe> recipeList = recipeRepository.findByIdMenuItemId(id);
        return recipeList.stream().map(recipeMapper::toDTO).collect(Collectors.toList());
    }
}
