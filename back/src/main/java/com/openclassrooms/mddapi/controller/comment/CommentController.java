package com.openclassrooms.mddapi.controller.comment;

import com.openclassrooms.mddapi.dto.comment.CreateCommentRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.CommentSummary;
import com.openclassrooms.mddapi.service.comment.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.openclassrooms.mddapi.config.OpenApiConfig.AUTH_COOKIE_SCHEME;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Commentaires", description = "Gestion des commentaires")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Crée un commentaire pour un article donné.
     *
     * @param postId identifiant de l'article cible
     * @param request contenu du commentaire
     * @return commentaire créé
     */
    @PostMapping("/{postId}/comments")
    @Operation(summary = "Ajouter un commentaire sur un article")
    @SecurityRequirement(name = AUTH_COOKIE_SCHEME)
    @ApiResponse(responseCode = "201", description = "Commentaire créé")
    @ApiResponse(responseCode = "400", description = "Requete invalide")
    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    @ApiResponse(responseCode = "404", description = "Article introuvable")
    public ResponseEntity<CommentSummary> createComment(@PathVariable Long postId,
                                                        @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(postId, request));
    }
}
