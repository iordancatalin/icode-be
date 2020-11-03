package com.icode.icodebe.repository;

import com.icode.icodebe.document.ResetPasswordToken;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class ResetPasswordTokenRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public ResetPasswordTokenRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<ResetPasswordToken> saveAndInvalidatePrevious(ResetPasswordToken resetPasswordToken) {
        final var criteria = Criteria.where("userId").is(resetPasswordToken.getUserId());
        final var query = new Query(criteria);

        final var update = new Update();
        update.set("valid", Boolean.FALSE);

        return reactiveMongoTemplate.updateMulti(query, update, ResetPasswordToken.class)
                .flatMap(unused -> reactiveMongoTemplate.save(resetPasswordToken));
    }

    public Mono<ResetPasswordToken> findByValidToken(String token) {
        final var criteria = new Criteria().andOperator(Criteria.where("token").is(token),
                Criteria.where("valid").is(Boolean.TRUE));
        final var query = new Query(criteria);

        return reactiveMongoTemplate.findOne(query, ResetPasswordToken.class);
    }

    public Mono<UpdateResult> invalidateToken(String token) {
        final var criteria = Criteria.where("token").is(token);
        final var query = new Query(criteria);

        final var update = new Update();
        update.set("valid", Boolean.FALSE);

        return reactiveMongoTemplate.updateFirst(query, update, ResetPasswordToken.class);
    }
}
