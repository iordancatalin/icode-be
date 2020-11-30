package com.icode.icodebe.service;

import com.icode.icodebe.document.Project;
import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.model.response.ProjectResponse;
import com.icode.icodebe.repository.ProjectRepository;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthenticationService authenticationService;

    public ProjectService(ProjectRepository projectRepository,
                          AuthenticationService authenticationService) {
        this.projectRepository = projectRepository;
        this.authenticationService = authenticationService;
    }

    public Mono<Void> saveOrUpdate(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest) {
        final var save = this.saveProject(saveOrUpdateProjectRequest).then(Mono.<UpdateResult>empty());

        return projectRepository.findByProjectRef(saveOrUpdateProjectRequest.getProjectRef())
                .flatMap(unused -> projectRepository.update(saveOrUpdateProjectRequest))
                .switchIfEmpty(save)
                .then();
    }

    public Flux<ProjectResponse> findProjectsForAuthenticatedUser() {
        return authenticationService.getAuthenticatedUser()
                .map(UserAccount::getId)
                .flatMapMany(projectRepository::findByOwnerId)
                .map(this::convertToProjectResponse);
    }

    public Mono<ProjectResponse> findByProjectRef(String projectRef) {
        return projectRepository.findByProjectRef(projectRef)
                .map(this::convertToProjectResponse);
    }

    public Mono<Void> deleteByProjectRef(String projectRef) {
        return projectRepository.deleteByProjectRef(projectRef).then();
    }

    private Mono<Project> saveProject(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest) {
        return authenticationService.getAuthenticatedUser()
                .map(UserAccount::getId)
                .map(id -> buildProject(saveOrUpdateProjectRequest, id))
                .flatMap(projectRepository::save);
    }

    private Project buildProject(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest, ObjectId ownerId) {
        return Project.builder()
                .directoryId(saveOrUpdateProjectRequest.getProjectRef())
                .name(saveOrUpdateProjectRequest.getProjectName())
                .ownerId(ownerId)
                .lastUpdated(LocalDateTime.now())
                .html(saveOrUpdateProjectRequest.getHtml())
                .css(saveOrUpdateProjectRequest.getCss())
                .js(saveOrUpdateProjectRequest.getJs())
                .build();
    }

    private ProjectResponse convertToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .name(project.getName())
                .lastUpdate(project.getLastUpdated())
                .projectRef(project.getDirectoryId())
                .html(project.getHtml())
                .css(project.getCss())
                .js(project.getJs())
                .build();
    }
}
