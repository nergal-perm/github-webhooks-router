package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.storage.TaskRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalWebhookStoreTest {

    @Test
    void save_shouldPassGeneratedFilenameToRepository() throws IOException {
        var capturedFilename = new String[1];
        TaskRepository fakeRepo = new FakeTaskRepository() {
            @Override
            public Path save(String filename, String content) {
                capturedFilename[0] = filename;
                return Path.of("/fake", filename);
            }
        };
        var fixedFilename = new WebhookFilename(
                Instant.parse("2026-01-24T10:00:00.000Z"),
                "my-repo",
                "abc12345"
        );
        var store = new LocalWebhookStore(fakeRepo, repoName -> fixedFilename);

        store.save("my-repo", "{}");

        assertThat(capturedFilename[0])
                .isEqualTo("2026-01-24T10:00:00.000Z_my-repo_abc12345.json");
    }

    @Test
    void save_shouldPassPayloadUnchangedToRepository() throws IOException {
        var capturedContent = new String[1];
        TaskRepository fakeRepo = new FakeTaskRepository() {
            @Override
            public Path save(String filename, String content) {
                capturedContent[0] = content;
                return Path.of("/fake", filename);
            }
        };
        var store = new LocalWebhookStore(fakeRepo, WebhookFilename::create);
        String payload = "{\"action\":\"opened\",\"number\":42}";

        store.save("repo", payload);

        assertThat(capturedContent[0]).isEqualTo(payload);
    }

    @Test
    void save_shouldReturnPathFromRepository() throws IOException {
        Path expectedPath = Path.of("/data/pending/test.json");
        TaskRepository fakeRepo = new FakeTaskRepository() {
            @Override
            public Path save(String filename, String content) {
                return expectedPath;
            }
        };
        var store = new LocalWebhookStore(fakeRepo, WebhookFilename::create);

        Path result = store.save("repo", "{}");

        assertThat(result).isEqualTo(expectedPath);
    }

    @Test
    void save_shouldUseRepoNameForFilenameGeneration() throws IOException {
        var capturedRepoName = new String[1];
        TaskRepository fakeRepo = new FakeTaskRepository() {
            @Override
            public Path save(String filename, String content) {
                return Path.of("/fake", filename);
            }
        };
        var store = new LocalWebhookStore(fakeRepo, repoName -> {
            capturedRepoName[0] = repoName;
            return new WebhookFilename(Instant.now(), repoName, "id");
        });

        store.save("owner/project", "{}");

        assertThat(capturedRepoName[0]).isEqualTo("owner/project");
    }

    private static abstract class FakeTaskRepository implements TaskRepository {
        @Override
        public Path save(String filename, String content) throws IOException {
            return Path.of("/fake", filename);
        }

        @Override
        public List<String> list() throws IOException {
            return List.of();
        }

        @Override
        public Path move(String filename, Path fromDir, Path toDir) throws IOException {
            return toDir.resolve(filename);
        }
    }
}
