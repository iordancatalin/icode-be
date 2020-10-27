package com.icode.icodebe.exception.handler.model.factory;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ErrorModelFactoryFacade implements ErrorModelFactory {

    private final ErrorModelFactory errorModelFactory;

    public ErrorModelFactoryFacade(@Value("${error.trace.enabled}") boolean value,
                                   @Value("${error.trace.disabledMessage}") String disabledMessage) {
        errorModelFactory = new ErrorModelFactoryImpl(value, disabledMessage);
    }

    @Override
    public ErrorModel createInternalServerError(Throwable throwable) {
        return errorModelFactory.createInternalServerError(throwable);
    }

    @Override
    public ErrorModel createBadRequest(Throwable throwable) {
        return errorModelFactory.createBadRequest(throwable);
    }

    @Override
    public ErrorModel createUnauthorized(Throwable throwable) {
        return errorModelFactory.createUnauthorized(throwable);
    }

    @Override
    public ErrorModel createForbidden(Throwable throwable) {
        return errorModelFactory.createForbidden(throwable);
    }

    @Override
    public ErrorModel createNotFound(Throwable throwable) {
        return errorModelFactory.createNotFound(throwable);
    }
}
