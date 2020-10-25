package com.icode.icodebe.exception.handler.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icode.icodebe.exception.handler.model.ErrorHandler;
import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;

import java.util.List;
import java.util.Objects;

import static java.util.List.*;
import static org.springframework.http.HttpStatus.*;

public class ErrorHandlerFactory {

    private final static List<Class<? extends Exception>> BAD_REQUEST_EXCEPTIONS = of(ValidationException.class, ServerWebInputException.class);
    private final static List<Class<? extends Exception>> UNAUTHORIZED_EXCEPTIONS = of(AuthenticationException.class);
    private final static List<Class<? extends Exception>> FORBIDDEN_EXCEPTIONS = of(AccessDeniedException.class);

    private static ErrorHandler<ServerWebExchange> createErrorHandleForInternalServerError(Throwable throwable) {
        final var errorModel = ErrorModelFactory.createInternalServerError(throwable);

        return createDefaultErrorHandler(INTERNAL_SERVER_ERROR, errorModel);
    }

    private static ErrorHandler<ServerWebExchange> createErrorHandleForBadRequest(Throwable throwable) {
        final var errorModel = ErrorModelFactory.createBadRequest(throwable);

        return createDefaultErrorHandler(BAD_REQUEST, errorModel);
    }

    private static ErrorHandler<ServerWebExchange> createErrorHandleForUnauthorized(Throwable throwable) {
        final var errorModel = ErrorModelFactory.createUnauthorized(throwable);

        return createDefaultErrorHandler(UNAUTHORIZED, errorModel);
    }

    private static ErrorHandler<ServerWebExchange> createErrorHandleForForbidden(Throwable throwable) {
        final var errorModel = ErrorModelFactory.createForbidden(throwable);

        return createDefaultErrorHandler(FORBIDDEN, errorModel);
    }

    public static ErrorHandler<ServerWebExchange> createErrorHandler(Throwable throwable) {
        if (isBadRequest(throwable)) {
            return createErrorHandleForBadRequest(throwable);
        }

        if (isUnauthorized(throwable)) {
            return createErrorHandleForUnauthorized(throwable);
        }

        if (isForbidden(throwable)) {
            return createErrorHandleForForbidden(throwable);
        }

        return createErrorHandleForInternalServerError(throwable);
    }

    private static boolean isUnauthorized(Throwable throwable) {
        return isThrowableInList(UNAUTHORIZED_EXCEPTIONS, throwable);
    }

    private static boolean isForbidden(Throwable throwable) {
        return isThrowableInList(FORBIDDEN_EXCEPTIONS, throwable);
    }

    private static boolean isBadRequest(Throwable throwable) {
        return isThrowableInList(BAD_REQUEST_EXCEPTIONS, throwable);
    }

    private static boolean isThrowableInList(List<Class<? extends Exception>> exceptions, Throwable throwable) {
        Objects.requireNonNull(throwable);
        return exceptions.stream()
                .anyMatch(clazz -> clazz.equals(throwable.getClass()));
    }

    private static ErrorHandler<ServerWebExchange> createDefaultErrorHandler(HttpStatus responseStatus,
                                                                             ErrorModel errorModel) {
        return serverWebExchange -> {
            final var response = serverWebExchange.getResponse();
            final var bufferFactory = response.bufferFactory();
            final var dataBuffer = bufferFactory.wrap(writeValueAsBytes(errorModel));

            response.setStatusCode(responseStatus);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return response.writeWith(Mono.just(dataBuffer));
        };
    }

    private static byte[] writeValueAsBytes(Object o) {
        try {
            return new ObjectMapper().writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new byte[]{};
    }
}
