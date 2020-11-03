package com.icode.icodebe.repository;

import com.icode.icodebe.document.UserAccount;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserAccountRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public UserAccountRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<UpdateResult> enableUserAccount(ObjectId id) {
        final var criteria = Criteria.where("id").is(id);
        final var query = Query.query(criteria);

        final var update = new Update();
        update.set("enabled", Boolean.TRUE);

        return reactiveMongoTemplate.updateFirst(query, update, UserAccount.class);
    }

    public Mono<UserAccount> findByConfirmationToken(String confirmationToken) {
        final var criteria = Criteria.where("confirmationToken").is(confirmationToken);
        final var query = Query.query(criteria);

        return reactiveMongoTemplate.findOne(query, UserAccount.class);
    }

    public Mono<UpdateResult> resetConfirmationToken(ObjectId id, String newToken) {
        final var criteria = Criteria.where("id").is(id);
        final var query = new Query(criteria);

        final var update = new Update();
        update.set("confirmationToken", newToken);

        return reactiveMongoTemplate.updateFirst(query, update, UserAccount.class);
    }

    public Mono<UserAccount> findByEmailOrUsername(String search) {
        return findByEmailOrUsername(search, search);
    }

    public Mono<UserAccount> findByEmailOrUsername(String email, String username) {
        final var criteria = new Criteria().orOperator(Criteria.where("email").is(email),
                Criteria.where("username").is(username));
        final var query = Query.query(criteria);

        return reactiveMongoTemplate.findOne(query, UserAccount.class);
    }

    public Mono<UserAccount> findById(ObjectId id) {
        final var criteria = Criteria.where("id").is(id);
        final var query = new Query(criteria);

        return reactiveMongoTemplate.findOne(query, UserAccount.class);
    }

    public Mono<UserAccount> save(UserAccount userAccount) {
        return reactiveMongoTemplate.save(userAccount);
    }

    public Mono<UpdateResult> updateUserPassword(ObjectId userId, String newPassword) {
        final var criteria = Criteria.where("id").is(userId);
        final var query = new Query(criteria);

        final var update = new Update();
        update.set("password", newPassword);

        return reactiveMongoTemplate.updateFirst(query, update, UserAccount.class);
    }
}
