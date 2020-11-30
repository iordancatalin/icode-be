package com.icode.icodebe.router;

import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.model.response.ProjectResponse;
import com.icode.icodebe.service.ProjectService;
import com.icode.icodebe.validator.ConstraintsValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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
        return nest(path("/api/v1"),
                route(POST("/project/save-or-update"), this::saveOrUpdateProject)
                        .andRoute(GET("/projects"), this::findUserProjects)
                        .andRoute(GET("/project/{projectRef}"), this::findByProjectRef)
                        .andRoute(DELETE("/project/{projectRef}"), this::deleteProject)
        );
    }

    private Mono<ServerResponse> findUserProjects(ServerRequest serverRequest) {
        final var projects = projectService.findProjectsForAuthenticatedUser();

        return ServerResponse.ok().body(projects, ProjectResponse.class);
    }

    private Mono<ServerResponse> saveOrUpdateProject(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SaveOrUpdateProjectRequest.class)
                .doOnNext(validator::validate)
                .flatMap(projectService::saveOrUpdate)
                .then(ServerResponse.ok().build());
    }

    private Mono<ServerResponse> findByProjectRef(ServerRequest serverRequest) {
        final var projectRef = serverRequest.pathVariable("projectRef");
        final var projectPublisher = projectService.findByProjectRef(projectRef);

        return ServerResponse.ok().body(projectPublisher, ProjectResponse.class);
    }

    private Mono<ServerResponse> deleteProject(ServerRequest serverRequest) {
        final var projectRef = serverRequest.pathVariable("projectRef");

        return projectService.deleteByProjectRef(projectRef)
                .flatMap(unused -> ServerResponse.ok().build());
    }
}
