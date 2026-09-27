package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.model.Ingredient;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientDTO toDto(Ingredient ingredient);
    Ingredient toEntity(IngredientDTO ingredientDTO);
}
