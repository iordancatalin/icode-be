package com.icode.icodebe.service;

import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.exception.EmailOrUsernameAlreadyExistsException;
import com.icode.icodebe.exception.InvalidConfirmationTokenException;
import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.model.response.ResetConfirmationTokenResponse;
import com.icode.icodebe.model.response.SignUpResponse;
import com.icode.icodebe.repository.UserAccountRepository;
import com.icode.icodebe.rest.NotificationServiceClient;
import com.icode.icodebe.transformer.UserAccountTransformer;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
                .flatMap(userAccount -> sendConfirmationEmail(userAccount.getEmail(), userAccount.getConfirmationToken())
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

    public Mono<ResetConfirmationTokenResponse> resendConfirmationEmail(String userId) {
        final var id = new ObjectId(userId);

        return userAccountRepository.findById(id).flatMap(userAccount -> {
            final var email = userAccount.getEmail();
            final var newToken = createConfirmationToken();

            final var resendConfirmationEmail = userAccountRepository.resetConfirmationToken(id, newToken)
                    .flatMap(updateResult -> sendConfirmationEmail(email, newToken))
                    .map(unused -> new ResetConfirmationTokenResponse(newToken));

            return Boolean.TRUE.equals(userAccount.getEnabled()) ? Mono.empty() : resendConfirmationEmail;
        });
    }

    private Mono<ClientResponse> sendConfirmationEmail(String email, String confirmationToken) {
        return notificationServiceClient.sendConfirmationEmail(email, confirmationToken);
    }

    private UserAccount createUserWithEncodedPassword(UserAccount userAccount) {
        return userAccount.withPassword(passwordEncoder.encode(userAccount.getPassword()));
    }

    private UserAccount createUserWithConfirmationToken(UserAccount userAccount) {
        return userAccount.withConfirmationToken(createConfirmationToken());
    }

    private String createConfirmationToken() {
        return UUID.randomUUID().toString();
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
