package com.openclassrooms.mddapi.controller.post;

import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse;
import com.openclassrooms.mddapi.dto.post.PostSummaryResponse;
import com.openclassrooms.mddapi.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.openclassrooms.mddapi.config.OpenApiConfig.AUTH_COOKIE_SCHEME;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Articles", description = "Gestion des articles et du feed")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Créer un article")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "201", description = "Article créé")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    @ApiResponse(responseCode = "404", description = "Sujet introuvable")
    // Crée un nouvel article pour l'utilisateur connecté.
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Recupérer le detail d'un article")
    @ApiResponse(responseCode = "200", description = "Article retourné")
    @ApiResponse(responseCode = "404", description = "Article introuvable")
    // Retourne le détail complet d'un article (avec commentaires).
    public PostDetailResponse getPost(@PathVariable Long postId) {
        return postService.getPostDetail(postId);
    }

    @GetMapping("/feed")
    @Operation(summary = "Recupérer le feed des articles")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "200", description = "Feed retourné")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    // Retourne le feed de l'utilisateur, trié par date selon le paramètre sort.
    public List<PostSummaryResponse> getFeed(@RequestParam(defaultValue = "newest") String sort) {
        return postService.getFeed(sort);
    }
}
