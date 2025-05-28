package com.app.TwoFactorAuthentication.dto.request;

public record VerifyEmailOtpRequest(
        String email,
        String otp
) {
}
