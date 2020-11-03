package com.icode.icodebe.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResetPassword {

    @NotBlank
    private String newPassword;
}
