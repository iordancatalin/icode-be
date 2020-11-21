package com.icode.icodebe.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtBlackList {

    @Id
    private ObjectId id;
    private String jwt;

    public JwtBlackList(String jwt) {
        this.jwt = jwt;
    }
}
