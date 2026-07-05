package com.openclassrooms.mddapi.service.auth;

import com.openclassrooms.mddapi.dto.auth.AuthResponse;
import com.openclassrooms.mddapi.dto.auth.CurrentUserResponse;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.dto.auth.UpdateProfileRequest;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.repository.user.UserRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import com.openclassrooms.mddapi.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        CurrentUserProvider currentUserProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Inscrit un nouvel utilisateur après vérification de l'unicité email et username.
     *
     * @param request données d'inscription (email, username, password)
     * @return token JWT + informations de l'utilisateur créé
     * @throws ConflictException si email ou username déjà utilisé
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ConflictException("Ce nom d'utilisateur est déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    /**
     * Connecte un utilisateur par email ou username.
     *
     * @param request identifiant (email ou username) et mot de passe
     * @return token JWT + informations de l'utilisateur
     * @throws UnauthorizedException si identifiants invalides
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrUsername(request.identifier(), request.identifier())
            .orElseThrow(() -> new UnauthorizedException("Identifiants invalides"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Identifiants invalides");
        }

        return buildAuthResponse(user);
    }

    /**
     * Retourne les informations de l'utilisateur actuellement authentifié.
     *
     * @return userId, username, email
     */
    public CurrentUserResponse me() {
        User user = currentUserProvider.getCurrentUser();
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     * Le mot de passe n'est modifié que s'il est fourni et non vide.
     *
     * @param request nouvelles valeurs email, username, password (optionnel)
     * @return nouveau token JWT + informations mises à jour
     * @throws ConflictException si email ou username déjà utilisé par un autre compte
     */
    public AuthResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        String requestedEmail = request.email().trim();
        String requestedUsername = request.username().trim();

        userRepository.findByEmail(requestedEmail)
            .filter(foundUser -> !foundUser.getId().equals(currentUser.getId()))
            .ifPresent(foundUser -> {
                throw new ConflictException("Cet email est déjà utilisé");
            });

        userRepository.findByUsername(requestedUsername)
            .filter(foundUser -> !foundUser.getId().equals(currentUser.getId()))
            .ifPresent(foundUser -> {
                throw new ConflictException("Ce nom d'utilisateur est dejà utilisé");
            });

        currentUser.setEmail(requestedEmail);
        currentUser.setUsername(requestedUsername);

        if (request.password() != null && !request.password().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(request.password()));
        }

        User savedUser = userRepository.save(currentUser);
        return buildAuthResponse(savedUser);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
            jwtService.generateToken(user.getEmail()),
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
}
