package com.app.TwoFactorAuthentication.exceptions.authExceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
