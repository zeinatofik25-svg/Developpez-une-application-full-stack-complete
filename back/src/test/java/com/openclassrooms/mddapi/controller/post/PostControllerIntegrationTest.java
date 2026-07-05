package com.openclassrooms.mddapi.controller.post;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openclassrooms.mddapi.dto.post.PostDetailResponse;
import com.openclassrooms.mddapi.dto.post.PostSummaryResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.security.CustomUserDetailsService;
import com.openclassrooms.mddapi.security.JwtAuthenticationFilter;
import com.openclassrooms.mddapi.service.post.PostService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void createPostShouldReturnCreated() throws Exception {
        PostDetailResponse response = new PostDetailResponse(
            5L,
            "Titre",
            "Contenu",
            LocalDateTime.now(),
            new PostDetailResponse.TopicSummary(2L, "Java", "Backend"),
            new PostDetailResponse.AuthorSummary(1L, "zeina"),
            List.of()
        );

        when(postService.createPost(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"title\": \"Titre\",
                      \"content\": \"Contenu\",
                      \"topicId\": 2
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.topic.name").value("Java"));
    }

    @Test
    void getFeedShouldReturnList() throws Exception {
        when(postService.getFeed("newest")).thenReturn(List.of(
            new PostSummaryResponse(1L, "T", "C", LocalDateTime.now(), 2L, "Java", 3L, "author")
        ));

        mockMvc.perform(get("/api/posts/feed").param("sort", "newest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createPostShouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"title\": \"\",
                      \"content\": \"\"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Requete invalide"));
    }
}
