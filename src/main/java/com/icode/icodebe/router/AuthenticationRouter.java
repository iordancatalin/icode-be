package com.icode.icodebe.router;

import com.icode.icodebe.model.request.RequestResetPassword;
import com.icode.icodebe.model.request.ResetPassword;
import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.model.response.ResetConfirmationTokenResponse;
import com.icode.icodebe.model.response.SignUpResponse;
import com.icode.icodebe.service.AuthenticationService;
import com.icode.icodebe.validator.ConstraintsValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthenticationRouter {

    private final ConstraintsValidator validator;
    private final AuthenticationService authenticationService;

    public AuthenticationRouter(ConstraintsValidator validator,
                                AuthenticationService authenticationService) {
        this.validator = validator;
        this.authenticationService = authenticationService;
    }

    @Bean
    public RouterFunction<ServerResponse> authRouter() {
        return nest(path("/api/v1"),
                route(POST("/sign-up"), this::handleSignUp)
                        .andRoute(PUT("/confirm-email/{confirmationToken}"), this::handleConfirmEmail)
                        .andRoute(PUT("/resend-confirmation-email/{input:.+}"), this::handleResendConfirmationEmail)
                        .andRoute(PUT("/reset-password/{resetToken}"), this::handleResetPassword)
                        .andRoute(POST("/request-reset-password"), this::handleRequestResetPassword)
        );
    }

    private Mono<ServerResponse> handleResendConfirmationEmail(ServerRequest serverRequest) {
        final var input = serverRequest.pathVariable("input");
        final var resentConfirmationEmail = authenticationService.resendConfirmationEmail(input);

        return ServerResponse.ok().body(resentConfirmationEmail, ResetConfirmationTokenResponse.class);
    }

    private Mono<ServerResponse> handleConfirmEmail(ServerRequest serverRequest) {
        final var confirmationToken = serverRequest.pathVariable("confirmationToken");

        return authenticationService.confirmEmail(confirmationToken)
                .flatMap(unused -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> handleSignUp(ServerRequest serverRequest) {
        final var createAccount = serverRequest.bodyToMono(SignUp.class)
                .doOnNext(validator::validate)
                .flatMap(authenticationService::createAccount);

        return ServerResponse.status(HttpStatus.CREATED)
                .body(createAccount, SignUpResponse.class);
    }

    private Mono<ServerResponse> handleRequestResetPassword(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RequestResetPassword.class)
                .doOnNext(validator::validate)
                .map(RequestResetPassword::getInput)
                .flatMap(authenticationService::requestResetPassword)
                .flatMap(unused -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> handleResetPassword(ServerRequest serverRequest) {
        final var resetToken = serverRequest.pathVariable("resetToken");

        return serverRequest.bodyToMono(ResetPassword.class)
                .doOnNext(validator::validate)
                .map(ResetPassword::getNewPassword)
                .flatMap(newPassword -> authenticationService.resetPassword(resetToken, newPassword))
                .flatMap(unused -> ServerResponse.ok().build());
    }
}
