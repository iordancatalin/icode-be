package com.icode.icodebe.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetConfirmationTokenResponse {
    private String newToken;
}
