package com.actisys.inventoryservice.exception;

public class IngredientNotFoundException extends RuntimeException {
    public IngredientNotFoundException(Long id) {
        super("Ingredient id " + id + " not found");
    }
}
