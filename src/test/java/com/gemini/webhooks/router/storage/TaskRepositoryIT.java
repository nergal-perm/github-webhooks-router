package com.gemini.webhooks.router.storage;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskRepositoryIT {


    @SneakyThrows
    @Test
    void should_ensure_the_target_folder_exists(@TempDir Path tempDir) {
        TaskRepository target = createTaskRepositoryFor(tempDir);

        final Path save = target.createPendingTask("temp.json", "{}");
        final Path targetPath = tempDir.resolve("data/pending/temp.json");

        assertThat(targetPath).exists();
        assertThat(Files.readString(targetPath)).isEqualTo("{}");
        assertThat(save).isEqualTo(targetPath);
    }

    @SneakyThrows
    @Test
    void list_Pending_shouldReturnMultipleFiles(@TempDir Path tempDir) {
        TaskRepository target = createTaskRepositoryFor(tempDir);
        target.createPendingTask("a.json", "{}");
        target.createPendingTask("b.json", "{}");

        List<String> result = target.listPending();

        assertThat(result).containsExactlyInAnyOrder("a.json", "b.json");
    }

    private static FileSystemTaskRepository createTaskRepositoryFor(Path tempDir) {
        return FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));
    }

    @SneakyThrows
    @Test
    void list_Pending_shouldReturnEmptyForEmptyDirectory(@TempDir Path tempDir) {
        TaskRepository target = createTaskRepositoryFor(tempDir);

        List<String> result = target.listPending();

        assertThat(result).isEmpty();
    }

    @SneakyThrows
    @Test
    void list_Pending_shouldReturnFilenamesNotPaths(@TempDir Path tempDir) {
        TaskRepository target = createTaskRepositoryFor(tempDir);
        target.createPendingTask("webhook.json", "{}");

        List<String> result = target.listPending();

        assertThat(result).containsExactly("webhook.json");
        assertThat(result.get(0)).doesNotContain("/");
    }

    @SneakyThrows
    @Test
    void list_Pending_shouldExcludeSubdirectories(@TempDir Path tempDir) {
        TaskRepository target = createTaskRepositoryFor(tempDir);
        target.createPendingTask("file.json", "{}");
        Files.createDirectory(tempDir.resolve("subdir"));

        List<String> result = target.listPending();

        assertThat(result).containsExactly("file.json");
    }
}
