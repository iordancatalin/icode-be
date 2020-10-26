package com.icode.icodebe.exception.handler.model.factory;

import com.icode.icodebe.exception.handler.model.ErrorCode;
import com.icode.icodebe.exception.handler.model.ErrorModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.icode.icodebe.exception.handler.model.ErrorCode.*;

public class ErrorModelFactory {

    public static ErrorModel createInternalServerError(Throwable throwable) {
        return createErrorModel(X_500, throwable.getMessage());
    }

    public static ErrorModel createBadRequest(Throwable throwable) {
        return createErrorModel(X_400, throwable.getMessage());
    }

    public static ErrorModel createUnauthorized(Throwable throwable) {
        return createErrorModel(X_401, throwable.getMessage());
    }

    public static ErrorModel createForbidden(Throwable throwable) {
        return createErrorModel(X_403, throwable.getMessage());
    }

    public static ErrorModel createNotFound(Throwable throwable) {
        return createErrorModel(X_404, throwable.getMessage());
    }

    private static ErrorModel createErrorModel(ErrorCode errorCode, String message) {
        final var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        final var time = formatter.format(LocalDateTime.now());

        return ErrorModel.builder()
                .errorCode(errorCode)
                .errorMessage(message)
                .errorOccurrenceDate(time)
                .build();
    }
}
