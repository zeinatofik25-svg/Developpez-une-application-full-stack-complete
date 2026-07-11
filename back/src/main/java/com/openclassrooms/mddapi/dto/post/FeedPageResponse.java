package com.openclassrooms.mddapi.dto.post;

import java.util.List;

public record FeedPageResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {
}