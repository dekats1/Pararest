package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.RecipeDTO;
import com.actisys.inventoryservice.model.Recipe;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = IngredientMapper.class)
public interface RecipeMapper {
    RecipeDTO toDTO(Recipe recipe);
}
