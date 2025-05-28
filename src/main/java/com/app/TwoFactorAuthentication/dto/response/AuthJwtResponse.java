package com.app.TwoFactorAuthentication.dto.response;

import com.app.TwoFactorAuthentication.entity.User;

public record AuthJwtResponse(
        String accessToken,
        String refreshToken,
        User user
) {
}
