package com.icode.icodebe.transformer;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.request.SignUp;
import reactor.core.publisher.Mono;

public class UserAccountTransformer {

    public Mono<UserAccount> fromSignUp(SignUp signUp) {
        return Mono.just(signUp)
                .map(signUpModel -> UserAccount.builder()
                        .email(signUp.getEmail())
                        .username(signUp.getUsername())
                        .password(signUp.getPassword())
                        .build());
    }
}
