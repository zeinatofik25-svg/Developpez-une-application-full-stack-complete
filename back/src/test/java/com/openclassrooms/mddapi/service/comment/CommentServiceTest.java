package com.openclassrooms.mddapi.service.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.dto.comment.CreateCommentRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.CommentSummary;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.comment.CommentRepository;
import com.openclassrooms.mddapi.repository.post.PostRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createCommentShouldReturnCreatedCommentSummary() {
        User user = new User();
        user.setId(4L);
        user.setUsername("zeina");

        Post post = new Post();
        post.setId(8L);

        Comment savedComment = new Comment();
        savedComment.setId(11L);
        savedComment.setContent("Merci pour ce post");
        savedComment.setCreatedAt(LocalDateTime.now());

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(8L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentSummary response = commentService.createComment(8L, new CreateCommentRequest("Merci pour ce post"));

        assertEquals(11L, response.id());
        assertEquals("zeina", response.author().username());
    }

    @Test
    void createCommentShouldThrowNotFoundWhenPostDoesNotExist() {
        User user = new User();
        user.setId(4L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> commentService.createComment(8L, new CreateCommentRequest("Merci")));
    }
}
