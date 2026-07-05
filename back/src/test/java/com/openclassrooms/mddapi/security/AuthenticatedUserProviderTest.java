package com.openclassrooms.mddapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.repository.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserProviderTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserOptionalShouldReturnEmptyWhenNoAuthentication() {
        AuthenticatedUserProvider provider = new AuthenticatedUserProvider(userRepository);

        assertEquals(Optional.empty(), provider.getCurrentUserOptional());
    }

    @Test
    void getCurrentUserShouldThrowWhenAuthenticationExistsButUserNotFound() {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("missing@example.com", "password", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmailOrUsername("missing@example.com", "missing@example.com"))
            .thenReturn(Optional.empty());

        AuthenticatedUserProvider provider = new AuthenticatedUserProvider(userRepository);

        assertThrows(UnauthorizedException.class, provider::getCurrentUser);
    }

    @Test
    void getCurrentUserShouldReturnUserWhenAuthenticatedAndFound() {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("user@example.com", "password", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setId(8L);
        user.setEmail("user@example.com");

        when(userRepository.findByEmailOrUsername("user@example.com", "user@example.com"))
            .thenReturn(Optional.of(user));

        AuthenticatedUserProvider provider = new AuthenticatedUserProvider(userRepository);

        assertEquals(8L, provider.getCurrentUser().getId());
    }
}
