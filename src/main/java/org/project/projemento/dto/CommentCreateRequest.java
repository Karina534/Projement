package org.project.projemento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "Comment content is required")
        @Size(max = 4000, message = "Comment content must be at most {max} characters")
        String content
) {
}
