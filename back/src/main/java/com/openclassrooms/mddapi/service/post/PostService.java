package com.openclassrooms.mddapi.service.post;

import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.FeedPageResponse;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.AuthorSummary;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.CommentSummary;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.TopicSummary;
import com.openclassrooms.mddapi.dto.post.PostSummaryResponse;
import com.openclassrooms.mddapi.entity.Comment;
import com.openclassrooms.mddapi.entity.Post;
import com.openclassrooms.mddapi.entity.Topic;
import com.openclassrooms.mddapi.entity.User;
import com.openclassrooms.mddapi.exception.ForbiddenException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.post.PostRepository;
import com.openclassrooms.mddapi.repository.subscription.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.topic.TopicRepository;
import com.openclassrooms.mddapi.security.CurrentUserProvider;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserProvider currentUserProvider;

    public PostService(PostRepository postRepository, TopicRepository topicRepository,
                       SubscriptionRepository subscriptionRepository, CurrentUserProvider currentUserProvider) {
        this.postRepository = postRepository;
        this.topicRepository = topicRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Crée un article pour l'utilisateur connecté dans le sujet donné.
     *
     * @param request titre, contenu et id du sujet
     * @return détail de l'article créé
     * @throws NotFoundException si le sujet est introuvable
     */
    public PostDetailResponse createPost(CreatePostRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        LOGGER.info("Création d'article demandée: userId={}, topicId={}", currentUser.getId(), request.topicId());

        Topic topic = topicRepository.findById(request.topicId())
            .orElseThrow(() -> new NotFoundException("Sujet introuvable"));

        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setTopic(topic);
        post.setAuthor(currentUser);

        Post savedPost = postRepository.save(post);
        LOGGER.info("Article créé: postId={}, userId={}, topicId={}", savedPost.getId(), currentUser.getId(), topic.getId());
        return mapToDetail(savedPost, Collections.emptyList());
    }

    /**
     * Retourne le détail complet d'un article avec ses commentaires.
     *
     * @param postId identifiant de l'article
     * @return article + auteur + sujet + liste des commentaires
     * @throws NotFoundException si l'article est introuvable
     */
    public PostDetailResponse getPostDetail(Long postId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Post post = postRepository.findDetailedById(postId)
            .orElseThrow(() -> new NotFoundException("Article introuvable"));

        boolean subscribed = subscriptionRepository.existsByUserIdAndTopicId(currentUser.getId(), post.getTopic().getId());
        if (!subscribed) {
            LOGGER.warn("Accès refusé au détail d'article: userId={}, postId={}, topicId={}",
                currentUser.getId(), postId, post.getTopic().getId());
            throw new ForbiddenException("Accès refusé: abonnez-vous au thème pour consulter cet article");
        }

        LOGGER.debug("Détail d'article récupéré: userId={}, postId={}", currentUser.getId(), postId);
        return mapToDetail(post, post.getComments());
    }

    /**
     * Retourne le feed des articles filtrés par les sujets auxquels l'utilisateur est abonné.
     *
     * @param sort "newest" (défaut) pour décroissant, "oldest" pour croissant
     * @param page index de page (base 0)
     * @param size taille de page
     * @return page triée des articles du feed
     */
    public FeedPageResponse<PostSummaryResponse> getFeed(String sort, int page, int size) {
        User currentUser = currentUserProvider.getCurrentUser();
        List<Long> topicIds = subscriptionRepository.findTopicIdsByUserId(currentUser.getId());
        if (topicIds.isEmpty()) {
            LOGGER.debug("Feed vide: aucun abonnement pour userId={}", currentUser.getId());
            return new FeedPageResponse<>(List.of(), page, size, 0, 0, false);
        }

        Sort.Direction direction = "oldest".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<PostSummaryResponse> feedPage = postRepository.findByTopicIdIn(topicIds, pageable)
            .map(this::mapToSummary);

        LOGGER.debug("Feed paginé: userId={}, page={}, size={}, returned={}, hasNext={}",
            currentUser.getId(), page, size, feedPage.getNumberOfElements(), feedPage.hasNext());

        return new FeedPageResponse<>(
            feedPage.getContent(),
            feedPage.getNumber(),
            feedPage.getSize(),
            feedPage.getTotalElements(),
            feedPage.getTotalPages(),
            feedPage.hasNext()
        );
    }

    private PostSummaryResponse mapToSummary(Post post) {
        return new PostSummaryResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getCreatedAt(),
            post.getTopic().getId(),
            post.getTopic().getName(),
            post.getAuthor().getId(),
            post.getAuthor().getUsername()
        );
    }

    private PostDetailResponse mapToDetail(Post post, List<Comment> comments) {
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getCreatedAt(),
            new TopicSummary(post.getTopic().getId(), post.getTopic().getName(), post.getTopic().getDescription()),
            new AuthorSummary(post.getAuthor().getId(), post.getAuthor().getUsername()),
            comments.stream()
                .map(comment -> new CommentSummary(
                    comment.getId(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    new AuthorSummary(comment.getAuthor().getId(), comment.getAuthor().getUsername())
                ))
                .toList()
        );
    }
}
