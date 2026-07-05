package com.openclassrooms.mddapi.dto.post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt,
    Long topicId,
    String topicName,
    Long authorId,
    String authorUsername
) {
}
