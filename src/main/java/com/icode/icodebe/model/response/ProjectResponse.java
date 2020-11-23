package com.icode.icodebe.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {

    private final String name;
    private final String ownerName;
    private final String ownerEmail;
    private final LocalDateTime lastUpdate;
    private final String projectRef;
}
