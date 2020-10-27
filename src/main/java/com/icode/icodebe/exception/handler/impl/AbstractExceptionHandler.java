package com.icode.icodebe.exception.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icode.icodebe.exception.handler.ExceptionHandler;
import com.icode.icodebe.exception.handler.model.ErrorModel;
import com.icode.icodebe.exception.handler.model.factory.ErrorModelFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Getter
public abstract class AbstractExceptionHandler<T extends Throwable> implements ExceptionHandler {

    @Autowired
    private ErrorModelFactory errorModelFactory;

    @Override
    public abstract boolean supports(Class<? extends Throwable> clazz);

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        Objects.requireNonNull(serverWebExchange);
        Objects.requireNonNull(throwable);

        final var response = serverWebExchange.getResponse();
        final var bufferFactory = response.bufferFactory();

        final var castThrowable = (T) throwable;
        final var errorModel = getErrorModel(castThrowable);
        final var dataBuffer = bufferFactory.wrap(writeValueAsBytes(errorModel));

        response.setStatusCode(getHttpStatus(castThrowable));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.just(dataBuffer));
    }

    private byte[] writeValueAsBytes(Object obj) {
        try {
            return new ObjectMapper().writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new byte[]{};
    }

    public abstract ErrorModel getErrorModel(T throwable);

    public abstract HttpStatus getHttpStatus(T throwable);
}
