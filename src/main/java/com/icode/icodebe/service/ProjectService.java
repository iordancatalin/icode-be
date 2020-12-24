package com.icode.icodebe.service;

import com.icode.icodebe.document.Project;
import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.model.request.ShareProject;
import com.icode.icodebe.model.response.ProjectResponse;
import com.icode.icodebe.repository.ProjectRepository;
import com.icode.icodebe.rest.NotificationServiceClient;
import com.icode.icodebe.rest.model.NotificationRequest;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserAccountService userAccountService;
    private final ProjectRepository projectRepository;
    private final AuthenticationService authenticationService;
    private final NotificationServiceClient notificationServiceClient;

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

    public Mono<Void> shareProjectWithUser(ShareProject shareProject) {
        return userAccountService.findByUsername(shareProject.getUsername())
                .map(UserAccount::getId)
                .flatMap(userId -> projectRepository.shareProject(shareProject.getProjectRef(), userId))
                .flatMap(unused -> authenticationService.getAuthenticatedUser())
                .map(UserAccount::getUsername)
                .flatMap(from -> projectRepository.findByProjectRef(shareProject.getProjectRef())
                        .map(project -> NotificationRequest.builder()
                                .from(from)
                                .to(shareProject.getUsername())
                                .projectName(project.getName())
                                .build()))
                .flatMap(notificationServiceClient::createAppNotification)
                .then();
    }

    public Flux<ProjectResponse> findSharedProjects() {
        return authenticationService.getAuthenticatedUser()
                .map(UserAccount::getId)
                .flatMapMany(projectRepository::findSharedProjects)
                .map(this::convertToProjectResponse);
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
