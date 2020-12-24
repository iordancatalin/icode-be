package com.icode.icodebe.service;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.response.UserAccountDetails;
import com.icode.icodebe.repository.UserAccountRepository;
import com.sun.el.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public Mono<UserAccountDetails> getUserAccountDetails(String usernameOrEmail) {
        return userAccountRepository.findByEmailOrUsername(usernameOrEmail)
                .map(this::buildUserAccountDetails);
    }

    public Mono<UserAccount> findByUsernameOrEmail(String usernameOrEmail) {
        return userAccountRepository.findByEmailOrUsername(usernameOrEmail);
    }

    public Mono<UserAccount> findByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }

    private UserAccountDetails buildUserAccountDetails(UserAccount userAccount) {
        return UserAccountDetails.builder()
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .imageURL(userAccount.getImageURL())
                .build();
    }

}
