package org.project.projemento.security.jwt;

public record TokensResponse(
        String accessToken, String accessTokenExpiry
) {
}