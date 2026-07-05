package com.openclassrooms.mddapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extrait le subject (email) d'un token JWT signé.
     *
     * @param token JWT Bearer
     * @return subject encodé dans le token
     */
    public String extractSubject(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    /**
     * Génère un token JWT valide 24 heures signé avec la clé applicative.
     *
     * @param subject email de l'utilisateur
     * @return JWT signé
     */
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(24, ChronoUnit.HOURS)))
            .signWith(signingKey)
            .compact();
    }

    /**
     * Vérifie que le subject du token correspond au username Spring Security.
     *
     * @param token token JWT à valider
     * @param userDetails utilisateur chargé depuis la base
     * @return true si le token est valide pour cet utilisateur
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String subject = extractSubject(token);
        return subject != null && subject.equals(userDetails.getUsername());
    }
}
