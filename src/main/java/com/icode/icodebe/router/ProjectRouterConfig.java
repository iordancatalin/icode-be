package com.icode.icodebe.router;

import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.service.ProjectService;
import com.icode.icodebe.validator.ConstraintsValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class ProjectRouterConfig {

    private final ProjectService projectService;
    private final ConstraintsValidator validator;

    public ProjectRouterConfig(ProjectService projectService,
                               ConstraintsValidator validator) {
        this.projectService = projectService;
        this.validator = validator;
    }

    @Bean
    public RouterFunction<ServerResponse> projectRouter() {
        return nest(path("/api/v1/project"),
                route(POST("/save-or-update"), this::saveOrUpdateProject));
    }

    private Mono<ServerResponse> saveOrUpdateProject(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SaveOrUpdateProjectRequest.class)
                .doOnNext(validator::validate)
                .flatMap(projectService::saveOrUpdate)
                .then(ServerResponse.ok().build());
    }
}
