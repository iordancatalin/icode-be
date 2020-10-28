package com.icode.icodebe.security;

import com.icode.icodebe.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Log4j2
public class JwtAuthenticationFilter implements WebFilter {

    private static final String AUTH_TOKEN_TYPE = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        final var request = serverWebExchange.getRequest();

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Request doesn't contains Authorization header");
            return webFilterChain.filter(serverWebExchange);
        }

        final var token = validateAndGetAuthorizationHeader(request.getHeaders());
        final var username = getUsername(token);

        final var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of());

        return webFilterChain.filter(serverWebExchange)
                .subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private String getUsername(String token) {
        try {
            return jwtService.getSubject(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid jwt");
        }
    }

    private String validateAndGetAuthorizationHeader(HttpHeaders headers) {
        final var authorizationHeader = headers.getFirst(AUTHORIZATION);

        if (Objects.isNull(authorizationHeader)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return authorizationHeader.replace(AUTH_TOKEN_TYPE, "");
    }
}
