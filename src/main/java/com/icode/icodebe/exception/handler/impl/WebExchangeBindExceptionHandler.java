package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import com.icode.icodebe.exception.handler.model.factory.ErrorModelFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;

@Component
public class WebExchangeBindExceptionHandler extends AbstractExceptionHandler<WebExchangeBindException> {
    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return WebExchangeBindException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(WebExchangeBindException webExchangeBindException) {
        final var statusCode = webExchangeBindException.getStatus();
        switch (statusCode) {
            case NOT_FOUND:
                return ErrorModelFactory.createNotFound(webExchangeBindException);
            case UNAUTHORIZED:
                return ErrorModelFactory.createUnauthorized(webExchangeBindException);
            case FORBIDDEN:
                return ErrorModelFactory.createForbidden(webExchangeBindException);
            case BAD_REQUEST:
                return ErrorModelFactory.createBadRequest(webExchangeBindException);
            default:
                return ErrorModelFactory.createInternalServerError(webExchangeBindException);
        }
    }

    @Override
    public HttpStatus getHttpStatus(WebExchangeBindException webExchangeBindException) {
        final var statusCode = webExchangeBindException.getStatus();
        switch (statusCode) {
            case NOT_FOUND: return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED: return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN: return HttpStatus.FORBIDDEN;
            case BAD_REQUEST: return HttpStatus.BAD_REQUEST;
            default: return HttpStatus.INTERNAL_SERVER_ERROR;
        }

    }
}
