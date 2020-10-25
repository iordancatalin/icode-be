package com.icode.icodebe.exception.handler.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ErrorCode {
    X_400("X-400"),
    X_401("X-401"),
    X_403("X-403"),
    X_500("X-500");

    @JsonValue
    private final String value;

    ErrorCode(String value) {
        this.value = value;
    }
}
