package com.icode.icodebe.security;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.repository.UserAccountRepository;
import com.icode.icodebe.security.model.CustomUserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CustomReactiveUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .map(this::createUserDetails)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }

    private UserDetails createUserDetails(UserAccount userAccount) {
        return new CustomUserDetails(userAccount);
    }
}
