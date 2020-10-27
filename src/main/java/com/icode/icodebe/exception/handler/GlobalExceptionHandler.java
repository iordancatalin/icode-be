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

@Log4j2
@Order(-2)
@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ErrorModelFactory errorModelFactory;
    private final List<ExceptionHandler> exceptionHandlers;

    public GlobalExceptionHandler(ErrorModelFactory errorModelFactory,
                                  List<ExceptionHandler> exceptionHandlers) {
        this.errorModelFactory = errorModelFactory;
        this.exceptionHandlers = exceptionHandlers;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        log.error(throwable);

        final var exceptionHandler = exceptionHandlers.stream()
                .filter(handler -> handler.supports(throwable.getClass()))
                .findFirst()
                .orElse(new InternalServerErrorHandler(errorModelFactory));

        return exceptionHandler.handle(serverWebExchange, throwable);
    }

    private static class InternalServerErrorHandler extends AbstractExceptionHandler<Throwable> {

        private final ErrorModelFactory errorModelFactory;

        public InternalServerErrorHandler(ErrorModelFactory errorModelFactory) {
            this.errorModelFactory = errorModelFactory;
        }

        @Override
        public boolean supports(Class<? extends Throwable> clazz) {
            return true;
        }

        @Override
        public ErrorModel getErrorModel(Throwable throwable) {
            return errorModelFactory.createInternalServerError(throwable);
        }

        @Override
        public HttpStatus getHttpStatus(Throwable throwable) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
