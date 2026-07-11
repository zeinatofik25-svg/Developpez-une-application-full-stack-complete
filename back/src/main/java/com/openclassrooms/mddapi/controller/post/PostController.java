package com.openclassrooms.mddapi.controller.post;

import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.FeedPageResponse;
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

    /**
     * Crée un nouvel article pour l'utilisateur authentifié.
     *
     * @param request charge utile de création d'article
     * @return détail de l'article créé
     */
    @PostMapping
    @Operation(summary = "Créer un article")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "201", description = "Article créé")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    @ApiResponse(responseCode = "404", description = "Sujet introuvable")
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    /**
     * Retourne le détail d'un article si l'utilisateur est abonné au thème associé.
     *
     * @param postId identifiant de l'article
     * @return détail de l'article
     */
    @GetMapping("/{postId}")
    @Operation(summary = "Recupérer le detail d'un article")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "200", description = "Article retourné")
    @ApiResponse(responseCode = "403", description = "Accès refusé (non abonné au thème)")
    @ApiResponse(responseCode = "404", description = "Article introuvable")
    public PostDetailResponse getPost(@PathVariable Long postId) {
        return postService.getPostDetail(postId);
    }

    /**
     * Retourne une page du feed filtré par abonnements avec tri temporel.
     *
     * @param sort ordre temporel "newest" ou "oldest"
     * @param page index de la page (base 0)
     * @param size taille de page
     * @return page de posts pour le scroll infini
     */
    @GetMapping("/feed")
    @Operation(summary = "Recupérer le feed des articles")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "200", description = "Feed retourné")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    public FeedPageResponse<PostSummaryResponse> getFeed(@RequestParam(defaultValue = "newest") String sort,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return postService.getFeed(sort, page, size);
    }
}
