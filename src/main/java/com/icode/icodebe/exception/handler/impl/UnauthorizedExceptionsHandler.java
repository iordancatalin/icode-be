package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class UnauthorizedExceptionsHandler extends AbstractExceptionHandler<Throwable> {

    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return AuthenticationException.class.equals(clazz) ||
                BadCredentialsException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(Throwable throwable) {
        return getErrorModelFactory().createUnauthorized(throwable);
    }

    @Override
    public HttpStatus getHttpStatus(Throwable throwable) {
        return HttpStatus.UNAUTHORIZED;
    }
}
