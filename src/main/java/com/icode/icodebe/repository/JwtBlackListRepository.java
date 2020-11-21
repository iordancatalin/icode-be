package com.icode.icodebe.repository;

import com.icode.icodebe.document.JwtBlackList;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class JwtBlackListRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public JwtBlackListRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<JwtBlackList> findByJwt(String jwt) {
        final var criteria = Criteria.where("jwt").is(jwt);
        final var query = new Query(criteria);

        return reactiveMongoTemplate.findOne(query, JwtBlackList.class);
    }

    public Mono<JwtBlackList> save(JwtBlackList jwtBlackList) {
        return reactiveMongoTemplate.save(jwtBlackList);
    }
}
