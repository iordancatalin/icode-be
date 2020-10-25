package com.icode.icodebe.repository;

import com.icode.icodebe.document.UserAccount;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserAccountRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;


    public UserAccountRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<UserAccount> findByEmailOrUsername(String email, String username) {
        final var criteria = Criteria.where("email").is(email)
                .orOperator(Criteria.where("username").is(username));
        final var query = Query.query(criteria);

        return reactiveMongoTemplate.findOne(query, UserAccount.class);
    }

    public Mono<UserAccount> save(UserAccount userAccount) {
        return reactiveMongoTemplate.save(userAccount);
    }
}
