package com.gemini.webhooks.router.storage;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MktmpResolver.class)
class TaskRepositoryTest {



    @Test
    void move_shouldRelocateFileToDestinationDirectory(@Mktmp Path tempDir) throws IOException {
        Path pending = tempDir.resolve("pending");
        Path processing = tempDir.resolve("processing");
        Files.createDirectories(pending);
        String filename = "2026-01-24T12:00:00.000Z_my-project_abc123.json";
        Files.writeString(pending.resolve(filename), "{}");
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        Path result = repository.move(filename, pending, processing);

        assertThat(result).isEqualTo(processing.resolve(filename));
        assertThat(Files.exists(processing.resolve(filename))).isTrue();
        assertThat(Files.exists(pending.resolve(filename))).isFalse();
    }

    @Test
    void move_shouldPreserveFileContent(@Mktmp Path tempDir) throws IOException {
        Path pending = tempDir.resolve("pending");
        Path processing = tempDir.resolve("processing");
        Files.createDirectories(pending);
        String filename = "webhook.json";
        String content = "{\"action\":\"opened\",\"number\":42}";
        Files.writeString(pending.resolve(filename), content);
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        repository.move(filename, pending, processing);

        assertThat(Files.readString(processing.resolve(filename))).isEqualTo(content);
    }

    @Test
    void move_shouldCreateDestinationDirectoryIfNotExists(@Mktmp Path tempDir) throws IOException {
        Path pending = tempDir.resolve("pending");
        Path completed = tempDir.resolve("completed");
        Files.createDirectories(pending);
        String filename = "webhook.json";
        Files.writeString(pending.resolve(filename), "{}");
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        repository.move(filename, pending, completed);

        assertThat(Files.exists(completed)).isTrue();
        assertThat(Files.exists(completed.resolve(filename))).isTrue();
    }

    @Test
    void move_shouldThrowNoSuchFileExceptionWhenSourceNotFound(@Mktmp Path tempDir) throws IOException {
        Path pending = tempDir.resolve("pending");
        Path processing = tempDir.resolve("processing");
        Files.createDirectories(pending);
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        assertThatThrownBy(() -> repository.move("nonexistent.json", pending, processing))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void move_shouldWorkForProcessingToCompleted(@Mktmp Path tempDir) throws IOException {
        Path processing = tempDir.resolve("processing");
        Path completed = tempDir.resolve("completed");
        Files.createDirectories(processing);
        String filename = "webhook.json";
        Files.writeString(processing.resolve(filename), "{}");
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        Path result = repository.move(filename, processing, completed);

        assertThat(result).isEqualTo(completed.resolve(filename));
        assertThat(Files.exists(completed.resolve(filename))).isTrue();
        assertThat(Files.exists(processing.resolve(filename))).isFalse();
    }

    @Test
    void move_shouldWorkForProcessingToFailed(@Mktmp Path tempDir) throws IOException {
        Path processing = tempDir.resolve("processing");
        Path failed = tempDir.resolve("failed");
        Files.createDirectories(processing);
        String filename = "webhook.json";
        Files.writeString(processing.resolve(filename), "{}");
        TaskRepository repository = FileSystemTaskRepository.create(FileBasedTasksConfig.create(tempDir));

        Path result = repository.move(filename, processing, failed);

        assertThat(result).isEqualTo(failed.resolve(filename));
        assertThat(Files.exists(failed.resolve(filename))).isTrue();
        assertThat(Files.exists(processing.resolve(filename))).isFalse();
    }
}
