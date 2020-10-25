package com.icode.icodebe.router;

import com.icode.icodebe.model.request.SignUp;
import com.icode.icodebe.service.AuthenticationService;
import com.icode.icodebe.validator.ConstraintsValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
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
                route(POST("/sign-up"), this::handleSignUp));
    }

    private Mono<ServerResponse> handleSignUp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SignUp.class)
                .doOnNext(validator::validate)
                .flatMap(authenticationService::createAccount)
                .flatMap(userAccount -> ServerResponse.status(HttpStatus.CREATED).build());
    }
}
