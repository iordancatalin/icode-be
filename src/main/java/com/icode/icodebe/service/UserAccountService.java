package com.icode.icodebe.service;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.response.UserAccountDetails;
import com.icode.icodebe.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    
    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public Mono<UserAccountDetails> getUserAccountDetails(String usernameOrEmail) {
        return userAccountRepository.findByEmailOrUsername(usernameOrEmail)
                .map(this::buildUserAccountDetails);
    }

    private UserAccountDetails buildUserAccountDetails(UserAccount userAccount) {
        return UserAccountDetails.builder()
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .imageURL(userAccount.getImageURL())
                .build();
    }
}
