package com.icode.icodebe.exception.handler;

import com.icode.icodebe.exception.handler.factory.ErrorHandlerFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Not finished TODO: Finish the implementation for global error handler.

//@Configuration
//@Order(-2)
@Log4j2
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        log.error(throwable.getClass());
        final var errorHandler = ErrorHandlerFactory.createErrorHandler(throwable);

        return errorHandler.handle(serverWebExchange);
    }
}
