package com.openclassrooms.mddapi.service.topic;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserProvider currentUserProvider;

    public TopicService(TopicRepository topicRepository, SubscriptionRepository subscriptionRepository,
                        CurrentUserProvider currentUserProvider) {
        this.topicRepository = topicRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Retourne tous les thèmes triés par nom, avec l'état d'abonnement pour l'utilisateur courant.
    public List<TopicResponse> getAllTopics() {
        Set<Long> subscribedTopicIds = currentUserProvider.getCurrentUserOptional()
            .map(user -> new HashSet<>(subscriptionRepository.findTopicIdsByUserId(user.getId())))
            .orElseGet(HashSet::new);

        return topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
            .map(topic -> toResponse(topic, subscribedTopicIds.contains(topic.getId())))
            .toList();
    }

    // Convertit une entité Topic en DTO de réponse API.
    public TopicResponse toResponse(Topic topic, boolean subscribed) {
        return new TopicResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            topic.getCreatedAt(),
            subscribed
        );
    }
}
