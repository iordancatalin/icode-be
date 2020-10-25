package com.icode.icodebe.document;

import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document
public class UserAccount {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String username;

    private String email;
    private String password;
    private Boolean enabled;
}
