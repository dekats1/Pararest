package com.actisys.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recipe")
public class Recipe {
    @EmbeddedId
    private RecipeId id;

    @MapsId("ingredientId")
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(name = "quantity")
    private Integer quantity;
}
