package com.openclassrooms.mddapi.service.comment;

import com.openclassrooms.mddapi.dto.comment.CreateCommentRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.AuthorSummary;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.CommentSummary;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.comment.CommentRepository;
import com.openclassrooms.mddapi.repository.post.PostRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CurrentUserProvider currentUserProvider;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                          CurrentUserProvider currentUserProvider) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Ajoute un commentaire sur un article au nom de l'utilisateur connecté.
     *
     * @param postId identifiant de l'article ciblé
     * @param request charge utile du commentaire
     * @return résumé du commentaire créé
     */
    public CommentSummary createComment(Long postId, CreateCommentRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        LOGGER.info("Création commentaire demandée: userId={}, postId={}", currentUser.getId(), postId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Article introuvable"));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setAuthor(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        LOGGER.info("Commentaire créé: commentId={}, postId={}, userId={}", savedComment.getId(), postId, currentUser.getId());
        return new CommentSummary(
            savedComment.getId(),
            savedComment.getContent(),
            savedComment.getCreatedAt(),
            new AuthorSummary(currentUser.getId(), currentUser.getUsername())
        );
    }
}
