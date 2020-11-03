package com.icode.icodebe.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordModel {

    private String email;
    private String resetToken;
}
