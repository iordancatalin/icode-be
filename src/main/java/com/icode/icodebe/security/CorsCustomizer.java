package com.icode.icodebe.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

public class CorsCustomizer implements Customizer<ServerHttpSecurity.CorsSpec> {

    @Override
    public void customize(ServerHttpSecurity.CorsSpec corsSpec) {
        final CorsConfigurationSource configurationSource = serverWebExchange -> {
            final var cc = new CorsConfiguration();
            cc.addAllowedOrigin("*");
            cc.addAllowedMethod("*");
            cc.addAllowedHeader("*");

            return cc;
        };

        corsSpec.configurationSource(configurationSource);
    }
}
