package com.actisys.inventoryservice.exception;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(Long id) {
        super("Could not find recipe with id " + id);
    }
}
