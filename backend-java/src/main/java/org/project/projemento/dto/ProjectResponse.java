package org.project.projemento.dto;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        Instant createdAt
) {
}
