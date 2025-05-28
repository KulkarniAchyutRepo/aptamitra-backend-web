package com.app.TwoFactorAuthentication.dto.response;

import java.time.LocalDateTime;

public record StandardApiResponse(
        boolean success,
        int status,
        String message,
        Object data,
        LocalDateTime timestamp
) {
}
