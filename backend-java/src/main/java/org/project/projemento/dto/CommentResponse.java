package org.project.projemento.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long taskId,
        UserResponse author,
        String content,
        Instant createdAt
) {
}
