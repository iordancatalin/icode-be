package com.icode.icodebe.exception;

public class InvalidConfirmationTokenException extends RuntimeException {

    public InvalidConfirmationTokenException(String confirmationToken) {
        super("Invalid confirmation token " + confirmationToken);
    }
}
