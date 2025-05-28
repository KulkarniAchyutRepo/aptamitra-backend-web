package com.app.TwoFactorAuthentication.exceptions.authExceptions;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(String message) {
        super(message);
    }
}
