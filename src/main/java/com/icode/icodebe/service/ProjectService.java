package com.icode.icodebe.service;

import com.icode.icodebe.document.Project;
import com.icode.icodebe.document.UserAccount;
import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.icode.icodebe.repository.ProjectRepository;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
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
                .flatMap(project -> updateProject(saveOrUpdateProjectRequest))
                .switchIfEmpty(save)
                .then();
    }

    private Mono<Project> saveProject(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest) {
        return authenticationService.getAuthenticatedUser()
                .map(UserAccount::getId)
                .map(id -> buildProject(saveOrUpdateProjectRequest, id))
                .flatMap(projectRepository::save);
    }

    private Mono<UpdateResult> updateProject(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest) {
        return this.projectRepository.updateLastUpdatedDate(saveOrUpdateProjectRequest.getProjectRef(), LocalDateTime.now());
    }

    private Project buildProject(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest, ObjectId ownerId) {
        return Project.builder()
                .directoryId(saveOrUpdateProjectRequest.getProjectRef())
                .name(saveOrUpdateProjectRequest.getProjectName())
                .ownerId(ownerId)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
