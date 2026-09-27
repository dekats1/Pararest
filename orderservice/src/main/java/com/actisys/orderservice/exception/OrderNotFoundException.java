package com.actisys.orderservice.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() {
        super("Cannot find this order");
    }
}
