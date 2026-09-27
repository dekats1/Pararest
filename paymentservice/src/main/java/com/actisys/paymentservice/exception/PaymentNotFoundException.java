package com.actisys.paymentservice.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Payment id " + id + " not found");
    }
    public PaymentNotFoundException() {
        super("Can not find payment with this data");
    }
}
