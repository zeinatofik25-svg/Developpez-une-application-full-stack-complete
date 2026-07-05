package com.openclassrooms.mddapi.service.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void subscribeShouldPersistSubscriptionAndReturnTopicResponse() {
        User user = new User();
        user.setId(5L);
        Topic topic = new Topic();
        topic.setId(2L);
        topic.setName("Java");
        topic.setDescription("Backend");
        topic.setCreatedAt(LocalDateTime.now());

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));
        when(subscriptionRepository.existsByUserIdAndTopicId(5L, 2L)).thenReturn(false);

        TopicResponse result = subscriptionService.subscribe(2L);

        assertEquals(2L, result.id());
        assertEquals(true, result.subscribed());
        verify(subscriptionRepository).save(any());
    }

    @Test
    void subscribeShouldThrowNotFoundWhenTopicDoesNotExist() {
        User user = new User();
        user.setId(5L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(topicRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subscriptionService.subscribe(2L));
    }

    @Test
    void subscribeShouldRejectDuplicateSubscription() {
        User user = new User();
        user.setId(5L);
        Topic topic = new Topic();
        topic.setId(2L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));
        when(subscriptionRepository.existsByUserIdAndTopicId(5L, 2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> subscriptionService.subscribe(2L));
    }

    @Test
    void unsubscribeShouldDoNothingWhenUserNotSubscribed() {
        User user = new User();
        user.setId(5L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(subscriptionRepository.existsByUserIdAndTopicId(5L, 2L)).thenReturn(false);

        subscriptionService.unsubscribe(2L);

        verify(subscriptionRepository, never()).deleteByUserIdAndTopicId(5L, 2L);
    }

    @Test
    void unsubscribeShouldDeleteWhenUserIsSubscribed() {
        User user = new User();
        user.setId(5L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(subscriptionRepository.existsByUserIdAndTopicId(5L, 2L)).thenReturn(true);

        subscriptionService.unsubscribe(2L);

        verify(subscriptionRepository).deleteByUserIdAndTopicId(5L, 2L);
    }
}
