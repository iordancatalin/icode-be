package com.icode.icodebe.service;

import com.icode.icodebe.common.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.nio.file.StandardOpenOption.*;

@Log4j2
@Service
public class FileService {

    @Value("${file.name}")
    private String fileName;

    public Mono<Path> saveContentToDisk(String content) {
        return Mono.fromCompletionStage(() -> writeToDisk(content));
    }

    private CompletableFuture<Path> writeToDisk(String content) {
        final var directoryName = generateDirectoryName();
        final var path = Paths.get(Constants.STORAGE_ROOT_DIRECTORY, directoryName, fileName);

        createDirectoriesIfDoesNotExists(path.getParent());

        return  writeContentToPath(path, content);
    }

    private CompletableFuture<Path> writeContentToPath(Path path, String content) {
        final var completableFuture = new CompletableFuture<Path>();

        try (final var channel = AsynchronousFileChannel.open(path, WRITE, CREATE_NEW)) {
            final var contentBytes = content.getBytes();
            final var byteBuffer = ByteBuffer.allocate(contentBytes.length);
            byteBuffer.put(contentBytes);
            byteBuffer.flip();

            final var completionHandler = new FileWriteCompletionHandler(path);
            channel.write(byteBuffer, 0, completableFuture, completionHandler);

        } catch (IOException e) {
            log.error(e);
            completableFuture.completeExceptionally(e);
        }

        return completableFuture;
    }

    private void createDirectoriesIfDoesNotExists(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateDirectoryName() {
        return String.valueOf(UUID.randomUUID());
    }

    @AllArgsConstructor
    private static class FileWriteCompletionHandler implements CompletionHandler<Integer, CompletableFuture<Path>> {

        private final Path path;

        @Override
        public void completed(Integer result, CompletableFuture<Path> completableFuture) {
            completableFuture.complete(path);
        }

        @Override
        public void failed(Throwable exc, CompletableFuture<Path> completableFuture) {
            completableFuture.completeExceptionally(exc);
        }
    }
}
