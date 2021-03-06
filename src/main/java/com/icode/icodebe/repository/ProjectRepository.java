package com.icode.icodebe.repository;

import com.icode.icodebe.document.Project;
import com.icode.icodebe.model.request.SaveOrUpdateProjectRequest;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Repository
public class ProjectRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public ProjectRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<Project> findByProjectRef(String directoryId) {
        final var query = new Query(Criteria.where("directoryId").is(directoryId));

        return reactiveMongoTemplate.findOne(query, Project.class);
    }

    public Mono<Project> save(Project project) {
        return reactiveMongoTemplate.save(project);
    }

    public Mono<UpdateResult> update(SaveOrUpdateProjectRequest saveOrUpdateProjectRequest) {
        final var directoryId = saveOrUpdateProjectRequest.getProjectRef();
        final var query = new Query(Criteria.where("directoryId").is(directoryId));

        final var update = new Update()
                .set("lastUpdated", LocalDateTime.now())
                .set("html", saveOrUpdateProjectRequest.getHtml())
                .set("css", saveOrUpdateProjectRequest.getCss())
                .set("js", saveOrUpdateProjectRequest.getJs())
                .set("name", saveOrUpdateProjectRequest.getProjectName());

        return reactiveMongoTemplate.updateFirst(query, update, Project.class);
    }

    public Flux<Project> findByOwnerId(ObjectId ownerId) {
        final var query = new Query(Criteria.where("ownerId").is(ownerId));

        return reactiveMongoTemplate.find(query, Project.class);
    }

    public Mono<DeleteResult> deleteByProjectRef(String projectRef) {
        final var query = new Query(Criteria.where("directoryId").is(projectRef));

        return reactiveMongoTemplate.remove(query, Project.class);
    }

    public Mono<UpdateResult> shareProject(String projectRef, ObjectId userId) {
        final var query = new Query(Criteria.where("directoryId").is(projectRef));
        final var update = new Update();

        update.addToSet("sharedWith", userId);

        return reactiveMongoTemplate.updateFirst(query, update, Project.class);
    }

    public Flux<Project> findSharedProjects(ObjectId id) {
        final var query = new Query();

        return reactiveMongoTemplate.find(query, Project.class)
                .filter(project -> Objects.nonNull(project.getSharedWith()) && project.getSharedWith().contains(id));
    }

}
