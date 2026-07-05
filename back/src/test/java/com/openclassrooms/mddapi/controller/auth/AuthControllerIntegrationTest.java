package com.openclassrooms.mddapi.controller.auth;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openclassrooms.mddapi.dto.auth.AuthResponse;
import com.openclassrooms.mddapi.dto.auth.CurrentUserResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.security.CustomUserDetailsService;
import com.openclassrooms.mddapi.security.JwtAuthenticationFilter;
import com.openclassrooms.mddapi.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void registerShouldReturnCreatedResponse() throws Exception {
        when(authService.register(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new AuthResponse("jwt", 1L, "zeina", "zeina@example.com"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"email\": \"zeina@example.com\",
                      \"username\": \"zeina\",
                      \"password\": \"Password1!\"
                    }
                    """))
            .andExpect(status().isCreated())
                    .andExpect(header().string("Set-Cookie", containsString("mdd-token=")))
            .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void loginShouldReturnOkResponse() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new AuthResponse("jwt", 1L, "zeina", "zeina@example.com"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"identifier\": \"zeina@example.com\",
                      \"password\": \"Password1!\"
                    }
                    """))
            .andExpect(status().isOk())
                    .andExpect(header().string("Set-Cookie", containsString("mdd-token=")))
            .andExpect(jsonPath("$.username").value("zeina"));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"email\": \"bad-email\",
                      \"username\": \"\",
                      \"password\": \"123\"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Requete invalide"))
            .andExpect(jsonPath("$.validationErrors").exists());
    }

        @Test
        void meShouldReturnCurrentUser() throws Exception {
          when(authService.me()).thenReturn(new CurrentUserResponse(1L, "zeina", "zeina@example.com"));

          mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("zeina"));
        }

        @Test
        void updateProfileShouldReturnUpdatedUser() throws Exception {
          when(authService.updateProfile(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new AuthResponse("jwt", 1L, "new-name", "new@example.com"));

          mockMvc.perform(put("/api/auth/me")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                {
                  \"email\": \"new@example.com\",
                  \"username\": \"new-name\",
                  \"password\": \"Password1!\"
                }
                """))
            .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("mdd-token=")))
            .andExpect(jsonPath("$.email").value("new@example.com"));
        }
}
