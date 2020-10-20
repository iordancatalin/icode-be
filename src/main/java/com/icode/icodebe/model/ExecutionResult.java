package com.icode.icodebe.model;

import lombok.Value;

@Value
public class ExecutionResult {

    private final String workingDirectory;
    private final String endpoint;
}
