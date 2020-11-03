package com.icode.icodebe.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
@Builder
public class ResetPasswordToken {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String token;

    @Indexed(unique = true)
    private ObjectId userId;

    private Boolean valid;
}
