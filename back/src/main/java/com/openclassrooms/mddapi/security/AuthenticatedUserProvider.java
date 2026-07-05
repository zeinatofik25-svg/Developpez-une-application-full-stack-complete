package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.repository.user.UserRepository;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public AuthenticatedUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // Retourne l'utilisateur authentifié ou déclenche une erreur 401.
    public User getCurrentUser() {
        return getCurrentUserOptional()
            .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
    }

    @Override
    // Tente de résoudre l'utilisateur courant depuis le SecurityContext (optionnel).
    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String principalName = authentication.getName();
        return userRepository.findByEmailOrUsername(principalName, principalName);
    }
}
