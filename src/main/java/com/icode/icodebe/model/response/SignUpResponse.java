package com.icode.icodebe.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResponse {

    private String userId;
    private String username;
    private String email;
}
