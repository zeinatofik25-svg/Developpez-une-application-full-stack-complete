package com.openclassrooms.mddapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthResponse(
    @JsonIgnore
    String token,
    Long userId,
    String username,
    String email
) {
}
