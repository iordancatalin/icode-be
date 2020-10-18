package com.icode.icodebe.service;

import com.icode.icodebe.model.ExecutionRequest;
import com.icode.icodebe.model.ExecutionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.icode.icodebe.common.Constants.FILE_TEMPLATE;

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

    public Mono<ExecutionResult> writeContent(String content) {
        return fileService.saveContentToDisk(content)
                .map(Path::getParent)
                .map(Path::getFileName)
                .map(Path::toString)
                .map(this::createLink);
    }

    public Mono<InputStream> getExecutionResult(String directoryName) {
        return fileService.getPathToFile(directoryName)
                .map(Path::toFile)
                .map(file -> {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    return null;
                });
    }

    private ExecutionResult createLink(String directoryName) {
        final var endpoint = executionResultEndpoint + directoryName;

        return new ExecutionResult(endpoint);
    }
}
