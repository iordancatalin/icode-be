package com.icode.icodebe.router;

import com.icode.icodebe.model.ExecutionRequest;
import com.icode.icodebe.model.ExecutionResult;
import com.icode.icodebe.service.CodeExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class CodeExecutionRouterConfig {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionRouterConfig(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @Bean
    public RouterFunction<ServerResponse> executionRouter() {
        return route(POST("/api/execute-code"), this::handleExecute);
    }

    private Mono<ServerResponse> handleExecute(ServerRequest serverRequest) {
       final var responsePub =  serverRequest.bodyToMono(ExecutionRequest.class)
                .map(codeExecutionService::createHTMLContent)
                .flatMap(codeExecutionService::writeContent);

        return ServerResponse.ok().body(responsePub, ExecutionResult.class);
    }
}
