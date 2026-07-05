package com.openclassrooms.mddapi.service.post;

import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.AuthorSummary;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.CommentSummary;
import com.openclassrooms.mddapi.dto.post.PostDetailResponse.TopicSummary;
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
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PostService {

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
        Topic topic = topicRepository.findById(request.topicId())
            .orElseThrow(() -> new NotFoundException("Sujet introuvable"));

        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setTopic(topic);
        post.setAuthor(currentUser);

        Post savedPost = postRepository.save(post);
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
        Post post = postRepository.findDetailedById(postId)
            .orElseThrow(() -> new NotFoundException("Article introuvable"));
        return mapToDetail(post, post.getComments());
    }

    /**
     * Retourne le feed des articles filtrés par les sujets auxquels l'utilisateur est abonné.
     *
     * @param sort "newest" (défaut) pour décroissant, "oldest" pour croissant
     * @return liste triée des articles du feed
     */
    public List<PostSummaryResponse> getFeed(String sort) {
        User currentUser = currentUserProvider.getCurrentUser();
        List<Long> topicIds = subscriptionRepository.findTopicIdsByUserId(currentUser.getId());
        if (topicIds.isEmpty()) {
            return List.of();
        }

        Sort.Direction direction = "oldest".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return postRepository.findByTopicIdIn(topicIds, Sort.by(direction, "createdAt")).stream()
            .map(this::mapToSummary)
            .toList();
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
