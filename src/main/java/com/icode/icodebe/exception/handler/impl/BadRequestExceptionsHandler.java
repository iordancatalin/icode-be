package com.icode.icodebe.exception.handler.impl;

import com.icode.icodebe.exception.EmailOrUsernameAlreadyExistsException;
import com.icode.icodebe.exception.InvalidConfirmationTokenException;
import com.icode.icodebe.exception.ResetPasswordTokenExpiredOrInvalidException;
import com.icode.icodebe.exception.handler.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ServerWebInputException;

import javax.validation.ValidationException;

@Component
public class BadRequestExceptionsHandler extends AbstractExceptionHandler<Throwable> {

    @Override
    public boolean supports(Class<? extends Throwable> clazz) {
        return MethodArgumentNotValidException.class.equals(clazz) ||
                ServerWebInputException.class.equals(clazz) ||
                ValidationException.class.equals(clazz) ||
                EmailOrUsernameAlreadyExistsException.class.equals(clazz) ||
                InvalidConfirmationTokenException.class.equals(clazz) ||
                ResetPasswordTokenExpiredOrInvalidException.class.equals(clazz);
    }

    @Override
    public ErrorModel getErrorModel(Throwable throwable) {
        return getErrorModelFactory().createBadRequest(throwable);
    }

    @Override
    public HttpStatus getHttpStatus(Throwable throwable) {
        return HttpStatus.BAD_REQUEST;
    }
}
