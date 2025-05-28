package com.app.TwoFactorAuthentication.dto.Admin;

public record AddOrRemoveMultipleRolesToUsersRequestBody(
        Long userId,
        Long[] roleIds
) {
}
