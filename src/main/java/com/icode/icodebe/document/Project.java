package com.icode.icodebe.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document
public class Project {

    @Id
    private ObjectId id;

    private String name;
    private ObjectId ownerId;
    private LocalDateTime lastUpdated;
    private String directoryId;
}
