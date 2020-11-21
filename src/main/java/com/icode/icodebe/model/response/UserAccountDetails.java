package com.icode.icodebe.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAccountDetails {

    private String username;
    private String email;
    private String imageURL;
}
