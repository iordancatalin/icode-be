package com.icode.icodebe.exception;

public class ResetPasswordTokenExpiredOrInvalidException extends RuntimeException {

    public ResetPasswordTokenExpiredOrInvalidException(String token) {
        super("Token: " + token + " is invalid or expired");
    }
}
