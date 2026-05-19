package org.project.projemento.security.jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JwtToken(
        UUID id,
        String subject,
        List<String> authorities,
        Instant createdAt,
        Instant expiresAt
) {
}