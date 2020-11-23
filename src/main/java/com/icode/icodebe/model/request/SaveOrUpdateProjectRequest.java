package com.icode.icodebe.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SaveOrUpdateProjectRequest {

    @NotBlank
    private final String projectRef;

    @NotBlank
    private final String projectName;
}
