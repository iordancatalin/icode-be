package com.icode.icodebe.exception.handler.model.factory;

import com.icode.icodebe.exception.handler.model.ErrorModel;

public interface ErrorModelFactory {

    ErrorModel createInternalServerError(Throwable throwable);

    ErrorModel createBadRequest(Throwable throwable);

    ErrorModel createUnauthorized(Throwable throwable);

    ErrorModel createForbidden(Throwable throwable);

    ErrorModel createNotFound(Throwable throwable);
}
