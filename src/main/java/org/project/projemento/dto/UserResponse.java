package org.project.projemento.dto;

import org.project.projemento.domain.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Instant createdAt
) {
}
