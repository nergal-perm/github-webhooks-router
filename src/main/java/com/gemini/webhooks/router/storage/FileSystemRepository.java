package com.gemini.webhooks.router.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

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

    @Override
    public List<String> list() throws IOException {
        ensureFolderExists();
        try (Stream<Path> paths = Files.list(Path.of(folder).toAbsolutePath())) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();
        }
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
