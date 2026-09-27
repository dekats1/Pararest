package com.actisys.inventoryservice.dto;

import com.actisys.inventoryservice.model.RecipeId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {
    private RecipeId id;
    private IngredientDTO ingredient;
    private Integer quantity;
}
