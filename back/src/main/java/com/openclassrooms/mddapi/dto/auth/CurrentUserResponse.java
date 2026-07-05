package com.openclassrooms.mddapi.dto.auth;

public record CurrentUserResponse(
    Long userId,
    String username,
    String email
) {
}