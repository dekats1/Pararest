package com.actisys.orderservice.exception;

public class CanNotCancelOrderNowException extends RuntimeException {
    public CanNotCancelOrderNowException() {
        super("Can not cancel order now. Please try again later.");
    }
}
