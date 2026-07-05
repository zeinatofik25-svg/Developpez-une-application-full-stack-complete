package com.openclassrooms.mddapi.service.subscription;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.entity.Subscription;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TopicRepository topicRepository;
    private final CurrentUserProvider currentUserProvider;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, TopicRepository topicRepository,
                               CurrentUserProvider currentUserProvider) {
        this.subscriptionRepository = subscriptionRepository;
        this.topicRepository = topicRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Crée un abonnement entre l'utilisateur courant et le thème demandé.
    public TopicResponse subscribe(Long topicId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new NotFoundException("Sujet introuvable"));

        if (subscriptionRepository.existsByUserIdAndTopicId(currentUser.getId(), topicId)) {
            throw new ConflictException("Utilisateur déjà abonné à ce sujet");
        }

        Subscription subscription = new Subscription();
        subscription.setUser(currentUser);
        subscription.setTopic(topic);
        subscriptionRepository.save(subscription);

        return new TopicResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            topic.getCreatedAt(),
            true
        );
    }

    // Supprime l'abonnement de l'utilisateur courant au thème demandé si présent.
    public void unsubscribe(Long topicId) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (!subscriptionRepository.existsByUserIdAndTopicId(currentUser.getId(), topicId)) {
            return;
        }
        subscriptionRepository.deleteByUserIdAndTopicId(currentUser.getId(), topicId);
    }
}
