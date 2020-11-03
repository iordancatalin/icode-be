package com.icode.icodebe.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RequestResetPassword {

    @NotBlank
    private String input;
}
