package com.icode.icodebe.router;

import com.icode.icodebe.model.response.UserAccountDetails;
import com.icode.icodebe.service.UserAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class UserAccountRouterConfig {

    private final UserAccountService userAccountService;

    public UserAccountRouterConfig(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Bean
    public RouterFunction<ServerResponse> userAccountRouter() {
        return nest(path("/api/v1"),
                route(GET("/account-details"), this::getAccountDetails)
        );
    }

    private Mono<ServerResponse> getAccountDetails(ServerRequest serverRequest) {
        final var accountDetails = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMap(userAccountService::getUserAccountDetails);

        return ServerResponse.ok().body(accountDetails, UserAccountDetails.class);
    }
}
