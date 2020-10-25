package com.icode.icodebe.service;

import com.icode.icodebe.model.request.ExecutionRequest;
import com.icode.icodebe.model.response.ExecutionResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import static com.icode.icodebe.common.Constants.FILE_TEMPLATE;

@Log4j2
@Service
public class CodeExecutionService {

    @Value("${execution.result.endpoint}")
    private String executionResultEndpoint;
    private final FileService fileService;

    public CodeExecutionService(FileService fileService) {
        this.fileService = fileService;
    }

    public String createHTMLContent(ExecutionRequest executionRequest) {
        final var html = executionRequest.getHtml();
        final var css = executionRequest.getCss();
        final var js = executionRequest.getJs();

        return String.format(FILE_TEMPLATE, css, html, js);
    }

    public Mono<ExecutionResult> writeContent(@NonNull String content, @Nullable String directory) {
        final var publisher = Objects.isNull(directory) ? fileService.saveContentToDisk(content) :
                fileService.saveContentToDisk(content, directory);

        return publisher.map(Path::getParent)
                .map(Path::getFileName)
                .map(Path::toString)
                .map(this::createLink);
    }

    public Mono<InputStream> getExecutionResult(String directoryName) {
        return fileService.getPathToFile(directoryName)
                .map(Path::toFile)
                .map(this::createInputStreamFromFile);
    }

    private InputStream createInputStreamFromFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private ExecutionResult createLink(String directoryName) {
        final var endpoint = executionResultEndpoint + directoryName;

        return new ExecutionResult(directoryName, endpoint);
    }
}
