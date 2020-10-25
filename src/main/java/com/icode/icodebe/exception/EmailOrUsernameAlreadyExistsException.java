package com.icode.icodebe.exception;

public class EmailOrUsernameAlreadyExistsException extends RuntimeException {

    public EmailOrUsernameAlreadyExistsException(String email, String username) {
        super("Email " + email + " or username " + username + " already exists");
    }
}
