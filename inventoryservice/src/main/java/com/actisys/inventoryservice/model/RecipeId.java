package com.actisys.inventoryservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeId implements Serializable {
    private Long menuItemId;
    private Long ingredientId;
}
