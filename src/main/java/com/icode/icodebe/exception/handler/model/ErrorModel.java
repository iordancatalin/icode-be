package com.icode.icodebe.exception.handler.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ErrorModel {

    ErrorCode errorCode;
    String errorMessage;
    String errorOccurrenceDate;
}
