package com.openclassrooms.mddapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    String email,
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    String username,
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
        message = "Le mot de passe doit contenir au moins une lettre, un chiffre et un caractère spécial"
    )
    String password
) {
}