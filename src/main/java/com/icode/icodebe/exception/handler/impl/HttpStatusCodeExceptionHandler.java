package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class HttpStatusCodeExceptionHandler extends AbstractExceptionHandler<HttpStatusCodeException> {

    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return HttpStatusCodeException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(HttpStatusCodeException httpStatusCodeException) {
        final var statusCode = httpStatusCodeException.getStatusCode();
        switch (statusCode) {
            case NOT_FOUND:
                return getErrorModelFactory().createNotFound(httpStatusCodeException);
            case UNAUTHORIZED:
                return getErrorModelFactory().createUnauthorized(httpStatusCodeException);
            case FORBIDDEN:
                return getErrorModelFactory().createForbidden(httpStatusCodeException);
            default:
                return getErrorModelFactory().createInternalServerError(httpStatusCodeException);
        }
    }

    @Override
    public HttpStatus getHttpStatus(HttpStatusCodeException httpStatusCodeException) {
        final var statusCode = httpStatusCodeException.getStatusCode();
        switch (statusCode) {
            case NOT_FOUND: return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED: return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN: return HttpStatus.FORBIDDEN;
            default: return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
