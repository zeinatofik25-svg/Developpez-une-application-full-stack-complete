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
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CurrentUserProvider currentUserProvider;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                          CurrentUserProvider currentUserProvider) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Ajoute un commentaire sur un article au nom de l'utilisateur connecté.
    public CommentSummary createComment(Long postId, CreateCommentRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Article introuvable"));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setAuthor(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return new CommentSummary(
            savedComment.getId(),
            savedComment.getContent(),
            savedComment.getCreatedAt(),
            new AuthorSummary(currentUser.getId(), currentUser.getUsername())
        );
    }
}
