package org.project.projemento.security.jwt.service;

import org.project.projemento.security.jwt.JwtToken;
import org.project.projemento.security.jwt.TokenUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.time.Instant;

public class JwtAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private final JdbcTemplate jdbcTemplate;

    public JwtAuthenticationUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof JwtToken token){
            return new TokenUser(token.subject(), "nopass", true,true,
                    !this.jdbcTemplate.queryForObject("""
                            select exists(select id from deactivated_tokens where id = ?)
                            """, Boolean.class, token.id()) &&
                            token.expiresAt().isAfter(Instant.now()), true,
                    token.authorities().stream().map(SimpleGrantedAuthority::new).toList(),
                    token);
        }
        return null;
    }
}
