package com.icode.icodebe.security;

import com.icode.icodebe.service.JwtService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.*;

@Log4j2
public class TokenGeneratorFilter implements WebFilter {

    private static final String SIGN_IN_PATH = "/api/v1/sign-in";
    private static final String AUTH_TOKEN_TYPE = "Basic ";
    private static final String CREDENTIALS_SEPARATOR = "\\.";

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final JwtService jwtService;

    public TokenGeneratorFilter(ReactiveAuthenticationManager reactiveAuthenticationManager,
                                JwtService jwtService) {
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        final var requestHeaders = serverWebExchange.getRequest().getHeaders();

        if (isNotRequestForAuthentication(serverWebExchange)) {
            log.info("Request is not for authentication");
            return webFilterChain.filter(serverWebExchange);
        }

        final var authorizationHeader = validateAndGetAuthorizationHeader(requestHeaders);
        final var credentials = authorizationHeader.split(CREDENTIALS_SEPARATOR);

        assertCredentials(credentials);

        final var username = decodeString(credentials[0]);
        final var password = decodeString(credentials[1]);

        final var authentication = new UsernamePasswordAuthenticationToken(username, password);
        final var responseHeaders = serverWebExchange.getResponse().getHeaders();

        return reactiveAuthenticationManager.authenticate(authentication)
                .map(Authentication::getName)
                .map(jwtService::generateJwt)
                .doOnNext(responseHeaders::setBearerAuth)
                .doOnNext(ignore -> responseHeaders.setAccessControlExposeHeaders(List.of(AUTHORIZATION)))
                .flatMap(auth -> Mono.empty());
    }

    private String validateAndGetAuthorizationHeader(HttpHeaders headers) {
        final var authorizationHeader = headers.getFirst(AUTHORIZATION);

        if (Objects.isNull(authorizationHeader)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return authorizationHeader.replace(AUTH_TOKEN_TYPE, "");
    }

    private void assertCredentials(String[] credentials) {
        if (credentials.length != 2) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private String decodeString(String string) {
        final var bites = Base64.getDecoder().decode(string);

        return new String(bites);
    }

    private boolean isNotRequestForAuthentication(ServerWebExchange serverWebExchange) {
        final var request = serverWebExchange.getRequest();

        return !request.getHeaders().containsKey(AUTHORIZATION) ||
                request.getMethod() != HttpMethod.POST ||
                !SIGN_IN_PATH.equals(request.getPath().toString());
    }
}
