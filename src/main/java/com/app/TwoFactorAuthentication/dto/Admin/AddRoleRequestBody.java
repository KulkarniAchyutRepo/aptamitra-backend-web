package com.app.TwoFactorAuthentication.dto.Admin;

public record AddRoleRequestBody(
        long userId,
        long roleId
) {
}
