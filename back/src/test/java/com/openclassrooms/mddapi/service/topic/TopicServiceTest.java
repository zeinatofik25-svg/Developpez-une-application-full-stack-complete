package com.openclassrooms.mddapi.service.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TopicService topicService;

    @Test
    void getAllTopicsShouldReturnTopicsUnsubscribedWhenNoUserIsAuthenticated() {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setDescription("All about Java");

        when(currentUserProvider.getCurrentUserOptional()).thenReturn(Optional.empty());
        when(topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(topic));

        List<TopicResponse> topics = topicService.getAllTopics();

        assertEquals(1, topics.size());
        assertEquals("Java", topics.get(0).name());
        assertEquals(false, topics.get(0).subscribed());
        verify(topicRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void getAllTopicsShouldMarkSubscribedTopicsForAuthenticatedUser() {
        User user = new User();
        user.setId(7L);

        Topic topic1 = new Topic();
        topic1.setId(1L);
        topic1.setName("Java");

        Topic topic2 = new Topic();
        topic2.setId(2L);
        topic2.setName("Spring");

        when(currentUserProvider.getCurrentUserOptional()).thenReturn(Optional.of(user));
        when(subscriptionRepository.findTopicIdsByUserId(7L)).thenReturn(List.of(2L));
        when(topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(topic1, topic2));

        List<TopicResponse> topics = topicService.getAllTopics();

        assertEquals(2, topics.size());
        assertEquals(false, topics.get(0).subscribed());
        assertEquals(true, topics.get(1).subscribed());
    }
}
