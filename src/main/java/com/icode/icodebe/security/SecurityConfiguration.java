package com.icode.icodebe.security;

import com.icode.icodebe.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final JwtService jwtService;

    public SecurityConfiguration(ReactiveUserDetailsService reactiveUserDetailsService,
                                 JwtService jwtService) {
        this.reactiveUserDetailsService = reactiveUserDetailsService;
        this.jwtService = jwtService;
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        final var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder());

        return authenticationManager;
    }

    @Bean
    public TokenGeneratorFilter tokenGeneratorFilter() {
        return new TokenGeneratorFilter(reactiveAuthenticationManager(), jwtService);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        final var publicPaths = new String[]{"/api/v1/execute-code",
                "/api/v1/execution-result/*",
                "/api/v1/sign-up",
                "/api/v1/confirm-email/*",
                "/api/v1/resend-confirmation-email/*",
                "/api/v1/request-reset-password",
                "/api/v1/reset-password/*"};

        return http
                .csrf()
                .disable()
                .cors(new CorsCustomizer())
                .authenticationManager(reactiveAuthenticationManager())
                .addFilterBefore(tokenGeneratorFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange()
                .pathMatchers(publicPaths)
                .permitAll()
                .anyExchange()
                .authenticated()
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
