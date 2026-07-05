package com.openclassrooms.mddapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("0123456789012345678901234567890123456789012345678901234567890123");
    }

    @Test
    void generateTokenAndExtractSubjectShouldWork() {
        String token = jwtService.generateToken("user@example.com");

        assertEquals("user@example.com", jwtService.extractSubject(token));
    }

    @Test
    void isTokenValidShouldReturnTrueWhenSubjectMatchesUser() {
        String token = jwtService.generateToken("user@example.com");
        UserDetails userDetails = User.withUsername("user@example.com").password("x").authorities("ROLE_USER").build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidShouldReturnFalseWhenSubjectDiffersFromUser() {
        String token = jwtService.generateToken("user@example.com");
        UserDetails userDetails = User.withUsername("other@example.com").password("x").authorities("ROLE_USER").build();

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void extractSubjectShouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.extractSubject("invalid.token.value"));
    }
}
