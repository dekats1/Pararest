package com.actisys.inventoryservice.service;

import com.actisys.inventoryservice.dto.AddIngredientDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;

import java.util.HashMap;
import java.util.List;

public interface IngredientService {
    List<IngredientDTO> getAll();

    IngredientDTO addNewIngredient(AddIngredientDTO ingredient);

    void deleteIngredient(Long id);

    void pickUpIngredients(HashMap<Long, Integer> ingredients);
}
