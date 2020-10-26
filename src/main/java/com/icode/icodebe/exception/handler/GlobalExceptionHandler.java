package com.icode.icodebe.exception.handler;

import com.icode.icodebe.exception.handler.impl.AbstractExceptionHandler;
import com.icode.icodebe.exception.handler.model.ErrorModel;
import com.icode.icodebe.exception.handler.model.factory.ErrorModelFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@Order(-2)
@Log4j2
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final List<ExceptionHandler> exceptionHandlers;

    public GlobalExceptionHandler(List<ExceptionHandler> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        log.error(throwable);

        final var exceptionHandler = exceptionHandlers.stream()
                .filter(handler -> handler.supports(throwable.getClass()))
                .findFirst()
                .orElse(new InternalServerErrorHandler());

        return exceptionHandler.handle(serverWebExchange, throwable);
    }

    private static class InternalServerErrorHandler extends AbstractExceptionHandler<Throwable> {

        @Override
        public boolean supports(Class<? extends Throwable> clazz) {
            return true;
        }

        @Override
        public ErrorModel getErrorModel(Throwable throwable) {
            return ErrorModelFactory.createInternalServerError(throwable);
        }

        @Override
        public HttpStatus getHttpStatus(Throwable throwable) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
