package com.openclassrooms.mddapi.controller.auth;

import com.openclassrooms.mddapi.dto.auth.AuthResponse;
import com.openclassrooms.mddapi.dto.auth.CurrentUserResponse;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.dto.auth.UpdateProfileRequest;
import com.openclassrooms.mddapi.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.openclassrooms.mddapi.config.OpenApiConfig.AUTH_COOKIE_SCHEME;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription et de connexion")
public class AuthController {

    private static final String AUTH_COOKIE_NAME = "mdd-token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscrit un nouvel utilisateur et démarre sa session via cookie HttpOnly.
     *
     * @param request données d'inscription
     * @return profil utilisateur créé
     */
    @PostMapping("/register")
    @Operation(summary = "Inscrire un utilisateur")
    @SecurityRequirements
    @ApiResponse(responseCode = "201", description = "Utilisateur inscrit")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "409", description = "Email ou pseudo déjà utilisé")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, createAuthCookie(response.token()).toString())
            .body(response);
    }

    /**
     * Authentifie un utilisateur et crée un cookie de session sécurisé.
     *
     * @param request identifiant et mot de passe
     * @return profil utilisateur authentifié
     */
    @PostMapping("/login")
    @Operation(summary = "Connecter un utilisateur")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Connexion réussie")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createAuthCookie(response.token()).toString())
            .body(response);
    }

    /**
     * Retourne le profil de l'utilisateur actuellement authentifié.
     *
     * @return profil courant
     */
    @GetMapping("/me")
    @Operation(summary = "Récuperer l'utilisateur connecté")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "200", description = "Utilisateur retourné")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    public CurrentUserResponse me() {
        return authService.me();
    }

    /**
     * Met à jour le profil du compte connecté et renouvelle la session.
     *
     * @param request payload de mise à jour
     * @return profil mis à jour
     */
    @PutMapping("/me")
    @Operation(summary = "Mettre à jour le profil utilisateur")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "200", description = "Profil mis à jour")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    @ApiResponse(responseCode = "409", description = "Email ou pseudo déjà utilisé")
    public ResponseEntity<AuthResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        AuthResponse response = authService.updateProfile(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, createAuthCookie(response.token()).toString())
            .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Fermer la session utilisateur")
    @SecurityRequirements
    @ApiResponse(responseCode = "204", description = "Session fermée")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, clearAuthCookie().toString())
            .build();
    }

    private ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(24 * 60 * 60)
            .build();
    }

    private ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(0)
            .build();
    }
}
