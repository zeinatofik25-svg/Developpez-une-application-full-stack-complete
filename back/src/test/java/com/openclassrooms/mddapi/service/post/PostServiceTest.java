package com.openclassrooms.mddapi.service.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse;
import com.openclassrooms.mddapi.dto.post.PostSummaryResponse;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.post.PostRepository;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private PostService postService;

    @Test
    void createPostShouldReturnPostDetails() {
        User user = new User();
        user.setId(9L);
        user.setUsername("zeina");

        Topic topic = new Topic();
        topic.setId(3L);
        topic.setName("Angular");
        topic.setDescription("Frontend");

        Post saved = new Post();
        saved.setId(10L);
        saved.setTitle("Post title");
        saved.setContent("Post content");
        saved.setTopic(topic);
        saved.setAuthor(user);
        saved.setComments(List.of());

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(topicRepository.findById(3L)).thenReturn(Optional.of(topic));
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        PostDetailResponse response = postService.createPost(new CreatePostRequest("Post title", "Post content", 3L));

        assertEquals(10L, response.id());
        assertEquals("Angular", response.topic().name());
    }

    @Test
    void createPostShouldThrowNotFoundWhenTopicDoesNotExist() {
        User user = new User();
        user.setId(9L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(topicRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> postService.createPost(new CreatePostRequest("Post title", "Post content", 3L)));
    }

    @Test
    void getPostDetailShouldReturnPostWithComments() {
        User author = new User();
        author.setId(4L);
        author.setUsername("author");

        User commenter = new User();
        commenter.setId(7L);
        commenter.setUsername("reader");

        Topic topic = new Topic();
        topic.setId(3L);
        topic.setName("Angular");
        topic.setDescription("Frontend");

        Comment comment = new Comment();
        comment.setId(33L);
        comment.setContent("Nice");
        comment.setAuthor(commenter);

        Post post = new Post();
        post.setId(10L);
        post.setTitle("Post title");
        post.setContent("Post content");
        post.setTopic(topic);
        post.setAuthor(author);
        post.setComments(List.of(comment));

        when(postRepository.findDetailedById(10L)).thenReturn(Optional.of(post));

        PostDetailResponse response = postService.getPostDetail(10L);

        assertEquals(10L, response.id());
        assertEquals(1, response.comments().size());
    }

    @Test
    void getPostDetailShouldThrowWhenPostDoesNotExist() {
        when(postRepository.findDetailedById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getPostDetail(10L));
    }

    @Test
    void getFeedShouldReturnEmptyWhenNoSubscriptions() {
        User user = new User();
        user.setId(9L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(subscriptionRepository.findTopicIdsByUserId(9L)).thenReturn(List.of());

        List<PostSummaryResponse> feed = postService.getFeed("newest");

        assertEquals(0, feed.size());
    }

    @Test
    void getFeedShouldReturnPostsFromSubscribedTopicsSortedNewestFirst() {
        User user = new User();
        user.setId(9L);

        Topic topic = new Topic();
        topic.setId(3L);
        topic.setName("Angular");

        User author = new User();
        author.setId(4L);
        author.setUsername("author");

        Post post = new Post();
        post.setId(10L);
        post.setTitle("Post title");
        post.setContent("Post content");
        post.setCreatedAt(LocalDateTime.now());
        post.setTopic(topic);
        post.setAuthor(author);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(subscriptionRepository.findTopicIdsByUserId(9L)).thenReturn(List.of(3L));
        when(postRepository.findByTopicIdIn(List.of(3L), Sort.by(Sort.Direction.DESC, "createdAt")))
            .thenReturn(List.of(post));

        List<PostSummaryResponse> feed = postService.getFeed("newest");

        assertEquals(1, feed.size());
        assertEquals("Angular", feed.get(0).topicName());
        verify(postRepository).findByTopicIdIn(List.of(3L), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void getFeedShouldUseAscendingSortForOldest() {
        User user = new User();
        user.setId(9L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(subscriptionRepository.findTopicIdsByUserId(9L)).thenReturn(List.of(3L));
        when(postRepository.findByTopicIdIn(List.of(3L), Sort.by(Sort.Direction.ASC, "createdAt")))
            .thenReturn(List.of());

        postService.getFeed("oldest");

        verify(postRepository).findByTopicIdIn(List.of(3L), Sort.by(Sort.Direction.ASC, "createdAt"));
    }
}
