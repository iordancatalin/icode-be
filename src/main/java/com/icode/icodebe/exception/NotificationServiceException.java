package com.icode.icodebe.exception;

import org.springframework.http.HttpStatus;

public class NotificationServiceException extends RuntimeException {

    public NotificationServiceException(HttpStatus httpStatus) {
        super("Notification service response has status " + httpStatus);
    }
}
