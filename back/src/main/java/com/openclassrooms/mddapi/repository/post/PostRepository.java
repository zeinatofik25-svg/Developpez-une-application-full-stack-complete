package com.openclassrooms.mddapi.repository.post;

import com.openclassrooms.mddapi.entity.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author", "topic"})
    List<Post> findByTopicIdIn(List<Long> topicIds, Sort sort);

    @EntityGraph(attributePaths = {"author", "topic", "comments", "comments.author"})
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findDetailedById(@Param("postId") Long postId);
}
