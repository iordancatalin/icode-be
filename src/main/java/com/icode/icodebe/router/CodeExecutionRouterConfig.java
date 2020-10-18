package com.icode.icodebe.router;

import com.icode.icodebe.model.ExecutionRequest;
import com.icode.icodebe.model.ExecutionResult;
import com.icode.icodebe.service.CodeExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
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
        return route(POST("/api/execute-code"), this::handleExecute)
                .andRoute(GET("/api/execution-result/{directoryName}"), this::handleGetExecutionResult);
    }

    private Mono<ServerResponse> handleExecute(ServerRequest serverRequest) {
        final var responsePub = serverRequest.bodyToMono(ExecutionRequest.class)
                .map(codeExecutionService::createHTMLContent)
                .flatMap(codeExecutionService::writeContent);

        return ServerResponse.ok().body(responsePub, ExecutionResult.class);
    }

    private Mono<ServerResponse> handleGetExecutionResult(ServerRequest serverRequest) {
        final var directoryName = serverRequest.pathVariable("directoryName");
        final var responsePub = codeExecutionService.getExecutionResult(directoryName)
                .map(InputStreamResource::new);

        return ServerResponse.ok().contentType(MediaType.TEXT_HTML)
                .body(responsePub, Resource.class);
    }

}
