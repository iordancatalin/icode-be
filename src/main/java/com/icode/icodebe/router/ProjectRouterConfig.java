package com.icode.icodebe.router;

import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.model.request.ShareProject;
import com.icode.icodebe.model.response.ProjectResponse;
import com.icode.icodebe.service.ProjectService;
import com.icode.icodebe.validator.ConstraintsValidator;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProjectRouterConfig {

    private final ProjectService projectService;
    private final ConstraintsValidator validator;

    @Bean
    public RouterFunction<ServerResponse> projectRouter() {
        return nest(path("/api/v1"),
                route(POST("/project/save-or-update"), this::saveOrUpdateProject)
                        .andRoute(GET("/projects"), this::findUserProjects)
                        .andRoute(GET("/project/{projectRef}"), this::findByProjectRef)
                        .andRoute(DELETE("/project/{projectRef}"), this::deleteProject)
                        .andRoute(PUT("/project/share"), this::shareProject)
                        .andRoute(GET("/projects/shared-with-me"), this::findSharedProjects)
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

    private Mono<ServerResponse> shareProject(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ShareProject.class)
                .doOnNext(validator::validate)
                .flatMap(projectService::shareProjectWithUser)
                .then(ServerResponse.ok().build());
    }

    private Mono<ServerResponse> findSharedProjects(ServerRequest serverRequest) {
        final var projects = projectService.findSharedProjects();
        return ServerResponse.ok().body(projects, ProjectResponse.class);
    }
}
