package com.gemini.webhooks.router.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemRepository implements TaskRepository {
    private final String folder;

    public static FileSystemRepository create(String folder) {
        return new FileSystemRepository(folder);
    }

    private FileSystemRepository(String folder) {
        this.folder = folder;
    }

    @Override
    public Path save(String filename, String content) throws IOException {
        ensureFolderExists();
        saveFile(filename, content);
        return Path.of(folder, filename);
    }

    private void saveFile(String filename, String content) throws IOException {
        final Path path = Path.of(folder).toAbsolutePath();
        Files.writeString(path.resolve(filename), content);
    }

    private void ensureFolderExists() throws IOException {
        final Path path = Path.of(folder).toAbsolutePath();
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}
