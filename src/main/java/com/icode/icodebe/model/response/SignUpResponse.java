package com.icode.icodebe.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResponse {

    private String username;
    private String email;
    private Boolean enabled;
    private String confirmationToken;
}
