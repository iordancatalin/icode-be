package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component("customResponseStatusExceptionHandler")
public class ResponseStatusExceptionHandler extends AbstractExceptionHandler<ResponseStatusException> {

    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return ResponseStatusException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(ResponseStatusException responseStatusException) {
        final var statusCode = responseStatusException.getStatus();
        switch (statusCode) {
            case NOT_FOUND:
                return getErrorModelFactory().createNotFound(responseStatusException);
            case UNAUTHORIZED:
                return getErrorModelFactory().createUnauthorized(responseStatusException);
            case FORBIDDEN:
                return getErrorModelFactory().createForbidden(responseStatusException);
            default:
                return getErrorModelFactory().createInternalServerError(responseStatusException);
        }
    }

    @Override
    public HttpStatus getHttpStatus(ResponseStatusException responseStatusException) {
        final var statusCode = responseStatusException.getStatus();
        switch (statusCode) {
            case NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
