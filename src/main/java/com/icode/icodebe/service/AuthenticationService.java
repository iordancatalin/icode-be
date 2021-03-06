package com.icode.icodebe.service;

import com.icode.icodebe.document.JwtBlackList;
import com.icode.icodebe.document.ResetPasswordToken;
import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.exception.EmailOrUsernameAlreadyExistsException;
import com.icode.icodebe.exception.InvalidConfirmationTokenException;
import com.icode.icodebe.exception.ResetPasswordTokenExpiredOrInvalidException;
import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.model.response.ResetConfirmationTokenResponse;
import com.icode.icodebe.model.response.SignUpResponse;
import com.icode.icodebe.repository.JwtBlackListRepository;
import com.icode.icodebe.repository.ResetPasswordTokenRepository;
import com.icode.icodebe.repository.UserAccountRepository;
import com.icode.icodebe.rest.NotificationServiceClient;
import com.icode.icodebe.rest.model.EmailConfirmationModel;
import com.icode.icodebe.rest.model.ResetPasswordModel;
import com.icode.icodebe.transformer.UserAccountTransformer;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final JwtBlackListRepository jwtBlackListRepository;

    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 UserAccountRepository userAccountRepository,
                                 NotificationServiceClient notificationServiceClient,
                                 ResetPasswordTokenRepository resetPasswordTokenRepository,
                                 JwtBlackListRepository jwtBlackListRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.notificationServiceClient = notificationServiceClient;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.jwtBlackListRepository = jwtBlackListRepository;
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
                .map(userAccount -> new UserAccountTransformer().toSignUpResponse(userAccount));
    }

    public Mono<UpdateResult> confirmEmail(String confirmationToken) {
        final var confirmationTokenNotFound = Mono.<UpdateResult>error(new InvalidConfirmationTokenException(confirmationToken));

        return userAccountRepository.findByConfirmationToken(confirmationToken)
                .map(UserAccount::getId)
                .flatMap(userAccountRepository::enableUserAccount)
                .switchIfEmpty(confirmationTokenNotFound);
    }

    public Mono<ResetConfirmationTokenResponse> resendConfirmationEmail(String input) {
        return userAccountRepository.findByEmailOrUsername(input).flatMap(userAccount -> {
            final var email = userAccount.getEmail();
            final var newToken = generateToken();
            final var id = userAccount.getId();

            final var resendConfirmationEmail = userAccountRepository.resetConfirmationToken(id, newToken)
                    .map(unused -> new EmailConfirmationModel(email, newToken))
                    .flatMap(notificationServiceClient::sendConfirmationEmail)
                    .map(unused -> new ResetConfirmationTokenResponse(newToken));

            return Boolean.TRUE.equals(userAccount.getEnabled()) ? Mono.empty() : resendConfirmationEmail;
        });
    }

    /**
     * @param input - the username or the email address of the user
     */
    public Mono<ClientResponse> requestResetPassword(String input) {
        final var token = generateToken();

        return userAccountRepository.findByEmailOrUsername(input).flatMap(userAccount -> {
            final var resetPasswordToken = ResetPasswordToken.builder()
                    .token(token)
                    .userId(userAccount.getId())
                    .valid(Boolean.TRUE)
                    .build();

            return resetPasswordTokenRepository.saveAndInvalidatePrevious(resetPasswordToken)
                    .map(resetPassToken -> new ResetPasswordModel(userAccount.getEmail(), resetPassToken.getToken()))
                    .flatMap(notificationServiceClient::sendResetPassword);
        });
    }

    public Mono<UpdateResult> resetPassword(String resetToken, String newPassword) {
        return resetPasswordTokenRepository.findByValidToken(resetToken)
                .flatMap(resetPasswordToken -> isResetTokenExpired(resetPasswordToken) ?
                        Mono.error(new ResetPasswordTokenExpiredOrInvalidException(resetToken)) :
                        resetPassword(resetPasswordToken.getUserId(), newPassword))
                .flatMap(unused -> resetPasswordTokenRepository.invalidateToken(resetToken))
                .switchIfEmpty(Mono.error(new ResetPasswordTokenExpiredOrInvalidException(resetToken)));
    }

    public Mono<JwtBlackList> signOut(String jwt) {
        return jwtBlackListRepository.save(new JwtBlackList(jwt));
    }

    public Mono<UserAccount> getAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userAccountRepository::findByEmailOrUsername);
    }

    private Mono<UpdateResult> resetPassword(ObjectId userId, String newPassword) {
        final var encodedPassword = passwordEncoder.encode(newPassword);

        return userAccountRepository.updateUserPassword(userId, encodedPassword);
    }

    private boolean isResetTokenExpired(ResetPasswordToken resetPasswordToken) {
        final var from = resetPasswordToken.getId()
                .getDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        final var minutes = ChronoUnit.MINUTES.between(from, LocalDateTime.now());

        return minutes > 60;
    }

    private Mono<ClientResponse> sendConfirmationEmail(String email, String confirmationToken) {
        final var emailConfirmationModel = new EmailConfirmationModel(email, confirmationToken);

        return notificationServiceClient.sendConfirmationEmail(emailConfirmationModel);
    }

    private UserAccount createUserWithEncodedPassword(UserAccount userAccount) {
        return userAccount.withPassword(passwordEncoder.encode(userAccount.getPassword()));
    }

    private UserAccount createUserWithConfirmationToken(UserAccount userAccount) {
        return userAccount.withConfirmationToken(generateToken());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
