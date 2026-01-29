package com.gemini.webhooks.router.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
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
}
