package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
