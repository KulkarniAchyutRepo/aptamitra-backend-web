package com.app.TwoFactorAuthentication.dto.request;

public record AuthenticatorValidateCodeRequest(
        String email,
        String code
) {
}
