package com.openclassrooms.mddapi.repository.subscription;

import com.openclassrooms.mddapi.entity.Subscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByUserIdAndTopicId(Long userId, Long topicId);

    Optional<Subscription> findByUserIdAndTopicId(Long userId, Long topicId);

    @Transactional
    void deleteByUserIdAndTopicId(Long userId, Long topicId);

    @Query("select s.topic.id from Subscription s where s.user.id = :userId")
    List<Long> findTopicIdsByUserId(@Param("userId") Long userId);
}
