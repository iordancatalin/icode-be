package com.icode.icodebe.exception.handler;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ExceptionHandler {

    boolean supports(Class<? extends Throwable> clazz);

    Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable exception);
}
