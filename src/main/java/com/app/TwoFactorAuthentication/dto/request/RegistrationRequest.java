package com.app.TwoFactorAuthentication.dto.request;

public record RegistrationRequest(
        String email,
        String name,
        String password
) {
}
