package com.app.TwoFactorAuthentication.exceptions.securityExceptions;

public class ForbiddenAction extends RuntimeException {
    public ForbiddenAction(String message) {
        super(message);
    }
}
