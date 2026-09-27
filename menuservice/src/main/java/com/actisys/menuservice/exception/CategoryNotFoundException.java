package com.actisys.menuservice.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Category id " + id + " not found");
    }
}
