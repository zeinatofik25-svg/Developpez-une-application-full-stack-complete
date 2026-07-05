package com.openclassrooms.mddapi.dto.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt,
    TopicSummary topic,
    AuthorSummary author,
    List<CommentSummary> comments
) {
    public record TopicSummary(Long id, String name, String description) {
    }

    public record AuthorSummary(Long id, String username) {
    }

    public record CommentSummary(Long id, String content, LocalDateTime createdAt, AuthorSummary author) {
    }
}
