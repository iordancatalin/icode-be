package com.icode.icodebe.model.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ShareProject {

    @NotBlank
    private String username;

    @NotBlank
    private String projectRef;
}
