package com.actisys.userservice.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("Cannot find user with id " + id);
    }
}
