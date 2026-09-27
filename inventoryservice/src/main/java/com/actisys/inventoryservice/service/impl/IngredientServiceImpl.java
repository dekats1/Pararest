package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddIngredientDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.exception.IngredientNotFoundException;
import com.actisys.inventoryservice.mapper.IngredientMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Override
    public List<IngredientDTO> getAll() {
        List<Ingredient> all = ingredientRepository.findAll();
        return all.stream().map(ingredientMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public IngredientDTO addNewIngredient(AddIngredientDTO ingredient) {
        Ingredient newIngredient = Ingredient.builder()
                .name(ingredient.getName())
                .unit(ingredient.getUnit())
                .currentStock(ingredient.getCurrentStock())
                .minStock(ingredient.getMinStock())
                .updatedAt(LocalDateTime.now())
                .build();
        return ingredientMapper.toDto(ingredientRepository.save(newIngredient));
    }

    @Override
    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id).orElseThrow(
                ()->new IngredientNotFoundException(id));
        ingredientRepository.delete(ingredient);
    }

    @Override
    public void pickUpIngredients(HashMap<Long, Integer> ingredients) {
        for(Map.Entry<Long, Integer> entry : ingredients.entrySet()) {
            Ingredient ingredient = ingredientRepository.findById(entry.getKey()).orElseThrow(
                    ()->new IngredientNotFoundException(entry.getKey()));
            ingredient.setCurrentStock(ingredient.getCurrentStock().subtract(BigDecimal.valueOf(entry.getValue())));
            ingredientRepository.save(ingredient);
        }
    }
}
