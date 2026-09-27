package com.actisys.orderservice.exception;

public class OrderInfoNotFoundException extends RuntimeException {
    public OrderInfoNotFoundException() {
        super("Can not find this OrderInfo");
    }
}
