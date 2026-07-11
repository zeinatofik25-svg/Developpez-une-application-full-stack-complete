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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final TopicRepository topicRepository;
    private final CurrentUserProvider currentUserProvider;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, TopicRepository topicRepository,
                               CurrentUserProvider currentUserProvider) {
        this.subscriptionRepository = subscriptionRepository;
        this.topicRepository = topicRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Crée un abonnement entre l'utilisateur courant et le thème demandé.
     *
     * @param topicId identifiant du thème à suivre
     * @return réponse du thème marqué comme abonné
     */
    public TopicResponse subscribe(Long topicId) {
        User currentUser = currentUserProvider.getCurrentUser();
        LOGGER.info("Demande d'abonnement: userId={}, topicId={}", currentUser.getId(), topicId);

        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new NotFoundException("Sujet introuvable"));

        if (subscriptionRepository.existsByUserIdAndTopicId(currentUser.getId(), topicId)) {
            throw new ConflictException("Utilisateur déjà abonné à ce sujet");
        }

        Subscription subscription = new Subscription();
        subscription.setUser(currentUser);
        subscription.setTopic(topic);
        subscriptionRepository.save(subscription);
        LOGGER.info("Abonnement créé: userId={}, topicId={}", currentUser.getId(), topicId);

        return new TopicResponse(
            topic.getId(),
            topic.getName(),
            topic.getDescription(),
            topic.getCreatedAt(),
            true
        );
    }

    /**
     * Supprime l'abonnement de l'utilisateur courant au thème demandé si présent.
     *
     * @param topicId identifiant du thème à ne plus suivre
     */
    public void unsubscribe(Long topicId) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (!subscriptionRepository.existsByUserIdAndTopicId(currentUser.getId(), topicId)) {
            LOGGER.debug("Aucun abonnement à supprimer: userId={}, topicId={}", currentUser.getId(), topicId);
            return;
        }
        subscriptionRepository.deleteByUserIdAndTopicId(currentUser.getId(), topicId);
        LOGGER.info("Abonnement supprimé: userId={}, topicId={}", currentUser.getId(), topicId);
    }
}
