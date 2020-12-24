package com.icode.icodebe.rest.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {

    private String from;
    private String to;
    private String projectName;
}
