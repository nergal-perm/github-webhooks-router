package com.gemini.webhooks.router.storage;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

public class FileSystemTaskRepository implements TaskRepository {
    private final static Logger logger = LoggerFactory.getLogger(FileSystemTaskRepository.class);
    private final FileBasedTasksConfig config;
    private final Path pendingDir;

    public static FileSystemTaskRepository create(FileBasedTasksConfig config) {
        return new FileSystemTaskRepository(config);
    }

    private FileSystemTaskRepository(FileBasedTasksConfig config) {
        this.config = config;
        this.pendingDir = config.pendingDir();
    }

    @Override
    public Path createPendingTask(String filename, String content) throws IOException {
        ensureFolderExists();
        saveFile(filename, content);
        return pendingDir.resolve(filename);
    }

    @Override
    public List<String> listPending() {
        return listDirectory(pendingDir);
    }

    @Override
    public List<String> listProcessing() {
        return listDirectory(config.processingDir());
    }

    @Override
    public Path move(String filename, Path fromDir, Path toDir) throws IOException {
        Path source = fromDir.toAbsolutePath().resolve(filename);
        if (Files.notExists(source)) {
            throw new NoSuchFileException(source.toString());
        }
        Path destinationDir = toDir.toAbsolutePath();
        if (Files.notExists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }
        Path destination = destinationDir.resolve(filename);
        Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        return destination;
    }

    private List<String> listDirectory(Path directory) {
        Path path = directory.toAbsolutePath();
        if (Files.notExists(path)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(path)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private void saveFile(String filename, String content) throws IOException {
        Files.writeString(pendingDir.toAbsolutePath().resolve(filename), content);
    }

    private void ensureFolderExists() throws IOException {
        Path path = pendingDir.toAbsolutePath();
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}
