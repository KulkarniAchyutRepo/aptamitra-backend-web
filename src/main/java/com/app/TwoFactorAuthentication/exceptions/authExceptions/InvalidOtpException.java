package com.app.TwoFactorAuthentication.exceptions.authExceptions;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
