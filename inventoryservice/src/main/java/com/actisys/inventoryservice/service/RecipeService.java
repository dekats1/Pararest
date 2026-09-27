package com.actisys.inventoryservice.service;

import com.actisys.dto.AddRecipeDTO;
import com.actisys.inventoryservice.dto.RecipeDTO;

import java.util.List;

public interface RecipeService {
    Boolean addNewRecipe(AddRecipeDTO addRecipeDTO);

    List<RecipeDTO> getRecipeByMenuId(Long id);
}
