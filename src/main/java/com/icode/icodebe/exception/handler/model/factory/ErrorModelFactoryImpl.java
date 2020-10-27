package com.icode.icodebe.exception.handler.model.factory;

import com.icode.icodebe.exception.handler.model.ErrorCode;
import com.icode.icodebe.exception.handler.model.ErrorModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.icode.icodebe.exception.handler.model.ErrorCode.*;


class ErrorModelFactoryImpl implements ErrorModelFactory {

    private final boolean withTrace;
    private final String traceDisabledMessage;

    public ErrorModelFactoryImpl(boolean withTrace, String traceDisabledMessage) {
        this.withTrace = withTrace;
        this.traceDisabledMessage = traceDisabledMessage;
    }

    public ErrorModel createInternalServerError(Throwable throwable) {
        return createErrorModel(X_500, throwable);
    }

    public ErrorModel createBadRequest(Throwable throwable) {
        return createErrorModel(X_400, throwable);
    }

    public ErrorModel createUnauthorized(Throwable throwable) {
        return createErrorModel(X_401, throwable);
    }

    public ErrorModel createForbidden(Throwable throwable) {
        return createErrorModel(X_403, throwable);
    }

    public ErrorModel createNotFound(Throwable throwable) {
        return createErrorModel(X_404, throwable);
    }

    private ErrorModel createErrorModel(ErrorCode errorCode, Throwable throwable) {
        final var time = calculateErrorOccurrenceDate();
        final var message = throwable.getMessage();
        final var stackTrace = withTrace ? convertStackTraceToString(throwable) : traceDisabledMessage;

        return ErrorModel.builder()
                .errorCode(errorCode)
                .errorMessage(message)
                .errorOccurrenceDate(time)
                .stackTrace(stackTrace)
                .build();
    }

    private String calculateErrorOccurrenceDate() {
        final var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return formatter.format(LocalDateTime.now());
    }

    private String convertStackTraceToString(Throwable throwable) {
        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        return sw.toString();
    }
}
