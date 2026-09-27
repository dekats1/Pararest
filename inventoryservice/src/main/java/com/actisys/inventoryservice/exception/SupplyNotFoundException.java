package com.actisys.inventoryservice.exception;

public class SupplyNotFoundException extends RuntimeException {
    public SupplyNotFoundException(Long id) {
        super("Supply not found: " + id);
    }
}
