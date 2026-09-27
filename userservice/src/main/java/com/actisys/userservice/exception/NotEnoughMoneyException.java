package com.actisys.userservice.exception;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException() {
        super("Not enough money on your balance");
    }
}
