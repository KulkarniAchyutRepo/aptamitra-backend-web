package com.app.TwoFactorAuthentication.exceptions.securityExceptions;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(String message){
        super(message);
    }
}
