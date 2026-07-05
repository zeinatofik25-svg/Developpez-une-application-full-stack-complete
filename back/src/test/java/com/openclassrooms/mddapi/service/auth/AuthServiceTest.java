package com.openclassrooms.mddapi.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.dto.auth.AuthResponse;
import com.openclassrooms.mddapi.dto.auth.CurrentUserResponse;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.dto.auth.UpdateProfileRequest;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import com.openclassrooms.mddapi.repository.user.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            new JwtService("0123456789012345678901234567890123456789012345678901234567890123"),
            currentUserProvider
        );
    }

    @Test
    void registerShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("user@example.com", "zeina", "Password1!");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.email());
        savedUser.setUsername(request.username());
        savedUser.setPassword("encoded-password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response.token());
        assertEquals("zeina", response.username());
        assertEquals("user@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowConflictWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("user@example.com", "zeina", "Password1!");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void registerShouldThrowConflictWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest("user@example.com", "zeina", "Password1!");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldThrowUnauthorizedWhenUserNotFound() {
        LoginRequest request = new LoginRequest("missing@example.com", "password");

        when(userRepository.findByEmailOrUsername(request.identifier(), request.identifier())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldThrowUnauthorizedWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("user@example.com", "bad-password");
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("zeina");
        user.setPassword("encoded-password");

        when(userRepository.findByEmailOrUsername(request.identifier(), request.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldReturnAuthResponseWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@example.com", "Password1!");
        User user = new User();
        user.setId(5L);
        user.setEmail("user@example.com");
        user.setUsername("zeina");
        user.setPassword("encoded-password");

        when(userRepository.findByEmailOrUsername(request.identifier(), request.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertEquals(5L, response.userId());
        assertEquals("zeina", response.username());
        assertNotNull(response.token());
    }

    @Test
    void meShouldReturnCurrentUserData() {
        User currentUser = new User();
        currentUser.setId(10L);
        currentUser.setUsername("zeina");
        currentUser.setEmail("zeina@example.com");
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        CurrentUserResponse response = authService.me();

        assertEquals(10L, response.userId());
        assertEquals("zeina", response.username());
        assertEquals("zeina@example.com", response.email());
    }

    @Test
    void updateProfileShouldUpdateEmailUsernameAndPassword() {
        User currentUser = new User();
        currentUser.setId(10L);
        currentUser.setUsername("zeina");
        currentUser.setEmail("old@example.com");
        currentUser.setPassword("old-encoded");

        UpdateProfileRequest request = new UpdateProfileRequest("new@example.com", "new-name", "Password1!");

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("new-name")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1!")).thenReturn("new-encoded");
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        AuthResponse response = authService.updateProfile(request);

        assertEquals("new@example.com", response.email());
        assertEquals("new-name", response.username());
        assertEquals("new-encoded", currentUser.getPassword());
    }

    @Test
    void updateProfileShouldThrowConflictWhenEmailBelongsToAnotherUser() {
        User currentUser = new User();
        currentUser.setId(1L);

        User anotherUser = new User();
        anotherUser.setId(2L);

        UpdateProfileRequest request = new UpdateProfileRequest("new@example.com", "same-name", null);

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(ConflictException.class, () -> authService.updateProfile(request));
    }
}
