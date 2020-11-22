package com.icode.icodebe.security;

import com.icode.icodebe.common.Constants;
import com.icode.icodebe.document.JwtBlackList;
import com.icode.icodebe.repository.JwtBlackListRepository;
import com.icode.icodebe.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    private final JwtService jwtService;
    private final JwtBlackListRepository jwtBlackListRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   JwtBlackListRepository jwtBlackListRepository) {
        this.jwtService = jwtService;
        this.jwtBlackListRepository = jwtBlackListRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        final var request = serverWebExchange.getRequest();

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Request doesn't contains Authorization header");
            return webFilterChain.filter(serverWebExchange);
        }

        final var token = validateAndGetAuthorizationHeader(request.getHeaders());

        final var authenticate = Mono.just(token)
                .map(this::getUsername)
                .map(username -> new UsernamePasswordAuthenticationToken(username, null, List.of()))
                .flatMap(authentication -> webFilterChain.filter(serverWebExchange)
                        .subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authentication)));

        final var unauthorized = Mono.just(serverWebExchange)
                .map(ServerWebExchange::getResponse)
                .doOnNext(response -> response.setStatusCode(HttpStatus.UNAUTHORIZED))
                .then();

        return jwtBlackListRepository.findByJwt(token)
                .defaultIfEmpty(new JwtBlackList(null))
                .flatMap(jwtBlackList -> Objects.nonNull(jwtBlackList.getJwt()) ? unauthorized : authenticate);
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

        return authorizationHeader.replace(Constants.AUTH_TOKEN_TYPE, "");
    }
}
