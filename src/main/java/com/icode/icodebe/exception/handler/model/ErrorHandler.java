package com.icode.icodebe.exception.handler.model;

import reactor.core.publisher.Mono;

public interface ErrorHandler<T> {
    Mono<Void> handle(T t);
}
