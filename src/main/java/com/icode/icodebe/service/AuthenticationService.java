package com.icode.icodebe.service;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.exception.EmailOrUsernameAlreadyExistsException;
import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.repository.UserAccountRepository;
import com.icode.icodebe.transformer.UserAccountTransformer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;

    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 UserAccountRepository userAccountRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
    }

    public Mono<?> createAccount(SignUp signUp) {
        final var email = signUp.getEmail();
        final var username = signUp.getUsername();

        final var saveAccount = new UserAccountTransformer().fromSignUp(signUp)
                .map(this::createUserWithEncodedPassword)
                .flatMap(userAccountRepository::save);

        return userAccountRepository.findByEmailOrUsername(email, username)
                .flatMap(userAccount -> Mono.error(new EmailOrUsernameAlreadyExistsException(email, username)))
                .switchIfEmpty(saveAccount);
    }

    private UserAccount createUserWithEncodedPassword(UserAccount userAccount) {
        return userAccount.withPassword(passwordEncoder.encode(userAccount.getPassword()));
    }
}
