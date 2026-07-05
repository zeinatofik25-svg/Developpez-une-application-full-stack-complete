package com.openclassrooms.mddapi.dto.topic;

import java.time.LocalDateTime;

public record TopicResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    boolean subscribed
) {
}
