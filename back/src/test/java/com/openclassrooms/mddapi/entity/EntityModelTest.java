package com.openclassrooms.mddapi.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityModelTest {

    @Test
    void userShouldHandleConstructorGettersSettersAndPrePersist() throws Exception {
        List<Post> posts = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<Subscription> subscriptions = new ArrayList<>();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        User user = new User(1L, "user@example.com", "zeina", "secret", createdAt, posts, comments, subscriptions);
        assertEquals(1L, user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("zeina", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(posts, user.getPosts());
        assertEquals(comments, user.getComments());
        assertEquals(subscriptions, user.getSubscriptions());

        user.setId(2L);
        user.setEmail("next@example.com");
        user.setUsername("next");
        user.setPassword("next-secret");
        user.setCreatedAt(createdAt.plusDays(1));
        user.setPosts(List.of());
        user.setComments(List.of());
        user.setSubscriptions(List.of());

        assertEquals(2L, user.getId());
        assertEquals("next@example.com", user.getEmail());
        assertEquals("next", user.getUsername());

        User empty = new User();
        Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(empty);
        assertNotNull(empty.getCreatedAt());
    }

    @Test
    void topicShouldHandleConstructorGettersSettersAndPrePersist() throws Exception {
        List<Post> posts = new ArrayList<>();
        List<Subscription> subscriptions = new ArrayList<>();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        Topic topic = new Topic(1L, "Java", "Backend", createdAt, posts, subscriptions);
        assertEquals(1L, topic.getId());
        assertEquals("Java", topic.getName());
        assertEquals("Backend", topic.getDescription());
        assertEquals(createdAt, topic.getCreatedAt());
        assertEquals(posts, topic.getPosts());
        assertEquals(subscriptions, topic.getSubscriptions());

        topic.setId(2L);
        topic.setName("Spring");
        topic.setDescription("Framework");
        topic.setCreatedAt(createdAt.plusDays(2));
        topic.setPosts(List.of());
        topic.setSubscriptions(List.of());

        assertEquals(2L, topic.getId());
        assertEquals("Spring", topic.getName());

        Topic empty = new Topic();
        Method onCreate = Topic.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(empty);
        assertNotNull(empty.getCreatedAt());
    }

    @Test
    void subscriptionPostAndCommentShouldHandleCommonAccessorsAndPrePersist() throws Exception {
        User user = new User();
        user.setId(10L);
        Topic topic = new Topic();
        topic.setId(20L);
        Post post = new Post();
        post.setId(30L);

        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        Subscription subscription = new Subscription(1L, now, user, topic);
        assertEquals(1L, subscription.getId());
        assertEquals(now, subscription.getCreatedAt());
        assertEquals(user, subscription.getUser());
        assertEquals(topic, subscription.getTopic());

        subscription.setId(2L);
        subscription.setCreatedAt(now.plusDays(1));
        subscription.setUser(user);
        subscription.setTopic(topic);
        assertEquals(2L, subscription.getId());

        Method subOnCreate = Subscription.class.getDeclaredMethod("onCreate");
        subOnCreate.setAccessible(true);
        Subscription emptySubscription = new Subscription();
        subOnCreate.invoke(emptySubscription);
        assertNotNull(emptySubscription.getCreatedAt());

        Post fullPost = new Post(3L, "Title", "Content", now, user, topic, List.of());
        assertEquals(3L, fullPost.getId());
        assertEquals("Title", fullPost.getTitle());
        assertEquals("Content", fullPost.getContent());
        assertEquals(user, fullPost.getAuthor());
        assertEquals(topic, fullPost.getTopic());

        fullPost.setComments(List.of());
        assertEquals(0, fullPost.getComments().size());

        Method postOnCreate = Post.class.getDeclaredMethod("onCreate");
        postOnCreate.setAccessible(true);
        Post emptyPost = new Post();
        postOnCreate.invoke(emptyPost);
        assertNotNull(emptyPost.getCreatedAt());

        Comment comment = new Comment(4L, "Hello", now, user, fullPost);
        assertEquals(4L, comment.getId());
        assertEquals("Hello", comment.getContent());
        assertEquals(user, comment.getAuthor());
        assertEquals(fullPost, comment.getPost());

        comment.setId(5L);
        comment.setContent("Updated");
        comment.setCreatedAt(now.plusDays(3));
        comment.setAuthor(user);
        comment.setPost(fullPost);
        assertEquals(5L, comment.getId());

        Method commentOnCreate = Comment.class.getDeclaredMethod("onCreate");
        commentOnCreate.setAccessible(true);
        Comment emptyComment = new Comment();
        commentOnCreate.invoke(emptyComment);
        assertNotNull(emptyComment.getCreatedAt());
    }
}
