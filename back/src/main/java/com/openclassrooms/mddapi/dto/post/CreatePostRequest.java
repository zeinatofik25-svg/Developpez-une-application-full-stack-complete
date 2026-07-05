package com.openclassrooms.mddapi.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
    @NotBlank(message = "Le titre est obligatoire")
    String title,
    @NotBlank(message = "Le contenu est obligatoire")
    String content,
    @NotNull(message = "Le topic est obligatoire")
    Long topicId
) {
}
