package com.icode.icodebe.service;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.exception.EmailOrUsernameAlreadyExistsException;
import com.icode.icodebe.exception.InvalidConfirmationTokenException;
import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.model.response.SignUpResponse;
import com.icode.icodebe.repository.UserAccountRepository;
import com.icode.icodebe.rest.NotificationServiceClient;
import com.icode.icodebe.transformer.UserAccountTransformer;
import com.mongodb.client.result.UpdateResult;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Supplier;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final NotificationServiceClient notificationServiceClient;

    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 UserAccountRepository userAccountRepository,
                                 NotificationServiceClient notificationServiceClient) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.notificationServiceClient = notificationServiceClient;
    }

    public Mono<SignUpResponse> createAccount(SignUp signUp) {
        final var email = signUp.getEmail();
        final var username = signUp.getUsername();

        final Mono<UserAccount> saveAccount = new UserAccountTransformer().fromSignUp(signUp)
                .map(this::createUserWithEncodedPassword)
                .map(this::createUserWithConfirmationToken)
                .flatMap(userAccountRepository::save);

        return userAccountRepository.findByEmailOrUsername(email, username)
                .flatMap(userAccount -> Mono.error(new EmailOrUsernameAlreadyExistsException(email, username)))
                .then(saveAccount)
                .flatMap(userAccount -> sendConfirmationEmail(userAccount)
                        .map(unused -> userAccount))
                .map(this::createResponse);
    }

    public Mono<UpdateResult> confirmEmail(String confirmationToken) {
        final var confirmationTokenNotFound = Mono.<UpdateResult>error(new InvalidConfirmationTokenException(confirmationToken));

        return userAccountRepository.findByConfirmationToken(confirmationToken)
                .map(UserAccount::getId)
                .flatMap(userAccountRepository::enableUserAccount)
                .switchIfEmpty(confirmationTokenNotFound);
    }

    private Mono<ClientResponse> sendConfirmationEmail(UserAccount userAccount) {
        final var email = userAccount.getEmail();
        final var confirmationToken = userAccount.getConfirmationToken();

        return notificationServiceClient.sendConfirmationEmail(email, confirmationToken);
    }

    private UserAccount createUserWithEncodedPassword(UserAccount userAccount) {
        return userAccount.withPassword(passwordEncoder.encode(userAccount.getPassword()));
    }

    private UserAccount createUserWithConfirmationToken(UserAccount userAccount) {
        final var confirmationToken = UUID.randomUUID().toString();

        return userAccount.withConfirmationToken(confirmationToken);
    }

    private SignUpResponse createResponse(UserAccount userAccount) {
        return SignUpResponse.builder()
                .email(userAccount.getEmail())
                .username(userAccount.getUsername())
                .confirmationToken(userAccount.getConfirmationToken())
                .enabled(userAccount.getEnabled())
                .build();
    }
}
