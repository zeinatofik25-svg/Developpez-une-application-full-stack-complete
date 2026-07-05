package com.openclassrooms.mddapi.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    String content
) {
}
