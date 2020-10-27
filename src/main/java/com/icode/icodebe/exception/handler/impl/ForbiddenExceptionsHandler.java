package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class ForbiddenExceptionsHandler extends AbstractExceptionHandler<Throwable> {

    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return AccessDeniedException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(Throwable throwable) {
        return getErrorModelFactory().createForbidden(throwable);
    }

    @Override
    public HttpStatus getHttpStatus(Throwable throwable) {
        return HttpStatus.FORBIDDEN;
    }
}
