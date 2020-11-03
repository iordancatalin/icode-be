package com.icode.icodebe.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class EmailConfirmationModel {

    private String email;
    private String confirmationToken;
}
