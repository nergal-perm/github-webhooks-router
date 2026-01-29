package com.gemini.webhooks.router.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskRepositoryIT {
    @SneakyThrows
    @Test
    void should_ensure_the_target_folder_exists() {
        Path tempFolder = Path.of("target/test-temp-folder").toAbsolutePath();
        TaskRepository target = FileSystemRepository.create(tempFolder.toString());

        final Path save = target.save("temp.json", "{}");
        final Path targetPath = tempFolder.resolve("temp.json");

        assertThat(targetPath).exists();
        assertThat(Files.readString(targetPath)).isEqualTo("{}");
        assertThat(save).isEqualTo(targetPath);
    }

    @SneakyThrows
    @Test
    void list_shouldReturnMultipleFiles(@TempDir Path tempDir) {
        TaskRepository target = FileSystemRepository.create(tempDir.toString());
        target.save("a.json", "{}");
        target.save("b.json", "{}");

        List<String> result = target.list();

        assertThat(result).containsExactlyInAnyOrder("a.json", "b.json");
    }

    @SneakyThrows
    @Test
    void list_shouldReturnEmptyForEmptyDirectory(@TempDir Path tempDir) {
        Path emptyDir = tempDir.resolve("empty-" + UUID.randomUUID());
        TaskRepository target = FileSystemRepository.create(emptyDir.toString());

        List<String> result = target.list();

        assertThat(result).isEmpty();
    }

    @SneakyThrows
    @Test
    void list_shouldReturnFilenamesNotPaths(@TempDir Path tempDir) {
        TaskRepository target = FileSystemRepository.create(tempDir.toString());
        target.save("webhook.json", "{}");

        List<String> result = target.list();

        assertThat(result).containsExactly("webhook.json");
        assertThat(result.get(0)).doesNotContain("/");
    }

    @SneakyThrows
    @Test
    void list_shouldExcludeSubdirectories(@TempDir Path tempDir) {
        TaskRepository target = FileSystemRepository.create(tempDir.toString());
        target.save("file.json", "{}");
        Files.createDirectory(tempDir.resolve("subdir"));

        List<String> result = target.list();

        assertThat(result).containsExactly("file.json");
    }
}
