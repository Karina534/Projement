package org.project.projemento.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.project.projemento.security.jwt.JwtToken;
import org.project.projemento.security.jwt.TokensResponse;
import org.project.projemento.security.jwt.factory.DefaultAccessTokenFactory;
import org.project.projemento.security.jwt.factory.DefaultRefreshTokenFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;

public class RequestJwtTokenFilter extends OncePerRequestFilter {
    @Getter
    @Setter
    private RequestMatcher requestMatcher = PathPatternRequestMatcher
            .withDefaults().matcher(HttpMethod.POST, "/api/users/login/token");

    @Getter
    @Setter
    private Function<Authentication, JwtToken> refreshTokenFactory = new DefaultRefreshTokenFactory();

    @Getter
    @Setter
    private Function<JwtToken, JwtToken> accessTokenFactory = new DefaultAccessTokenFactory();

    @Getter
    @Setter
    private Function<JwtToken, String> accessTokenSerializer = Objects::toString;

    @Getter
    @Setter
    private Function<JwtToken, String> refreshTokenSerializer = Objects::toString;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.requestMatcher.matches(request)){
            var context = SecurityContextHolder.getContext();
            if (context != null && context.getAuthentication() != null){
                if (!(context.getAuthentication() instanceof AnonymousAuthenticationToken)){

                    var authentication = context.getAuthentication();
                    var refreshToken = this.refreshTokenFactory.apply(authentication);
                    var accessToken = this.accessTokenFactory.apply(refreshToken);

                    var cookie = new Cookie("refresh-token",
                            this.refreshTokenSerializer.apply(refreshToken));
                    cookie.setHttpOnly(true);
                    cookie.setPath("/auth");
                    cookie.setSecure(false);
                    cookie.setMaxAge((int) ChronoUnit.SECONDS.between(Instant.now(),
                            refreshToken.expiresAt()));

                    var tokensResponse = new TokensResponse(
                            this.accessTokenSerializer.apply(accessToken),
                            accessToken.expiresAt().toString()
                    );

                    response.addCookie(cookie);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    this.objectMapper.writeValue(response.getWriter(), tokensResponse);
                    return;
                }
            }

            throw new AccessDeniedException("User must be authenticated");
        }

        filterChain.doFilter(request, response);
    }
}
