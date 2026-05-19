package org.project.projemento.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.project.projemento.security.jwt.JwtToken;
import org.project.projemento.security.jwt.TokensResponse;
import org.project.projemento.security.jwt.factory.DefaultAccessTokenFactory;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RefreshJwtTokenFilter extends OncePerRequestFilter {
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/auth/refresh/token");

    @Setter
    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    @Setter
    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    @Setter
    private Function<String, JwtToken> refreshTokenDeserializer;

    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();

    private final JdbcTemplate jdbcTemplate;

    public RefreshJwtTokenFilter(Function<String, JwtToken> refreshTokenDeserializer, JdbcTemplate jdbcTemplate) {
        this.refreshTokenDeserializer = refreshTokenDeserializer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (requestMatcher.matches(request)){
                if (request.getCookies() != null){
                    Optional<Cookie> cookieStream = Stream.of(request.getCookies())
                            .filter(cookie -> cookie.getName().equals("refresh-token"))
                            .findFirst();

                    if (cookieStream.isEmpty()){
                        throw new AccessDeniedException("User must have refresh token in cookie");
                    }

                    Cookie cookie = cookieStream.get();
                    var refreshToken = this.refreshTokenDeserializer.apply(cookie.getValue());
                    validateRefreshToken(refreshToken);
                    var accessToken = this.accessTokenFactory.apply(refreshToken);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(),
                            new TokensResponse((this.accessTokenSerializer.apply(accessToken)),
                                    accessToken.expiresAt().toString()
                            ));
                    return;
                }

                throw new AccessDeniedException("User must be authenticated with Jwt");
            }
        filterChain.doFilter(request, response);
    }

    private void validateRefreshToken(JwtToken refreshToken) {
        if (refreshToken == null
                || refreshToken.expiresAt().isBefore(Instant.now())
                || !refreshToken.authorities().contains("ROLE_REFRESH")
                || isDeactivated(refreshToken)) {
            throw new AccessDeniedException("Refresh token is invalid");
        }
    }

    private boolean isDeactivated(JwtToken token) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists(select id from deactivated_tokens where id = ?)
                """, Boolean.class, token.id());
        return Boolean.TRUE.equals(exists);
    }
}
