package com.gemini.webhooks.router.download;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.gemini.webhooks.router.domain.WebhookPayload;
import com.gemini.webhooks.router.domain.WebhookRecord;
import com.gemini.webhooks.router.storage.FileSystemTaskRepository;
import com.gemini.webhooks.router.storage.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DownloaderTest {

    // deliveryId whose first 8 hex chars (dashes removed) are "72d3162e"
    private static final String DELIVERY_ID = "72d3162e-cc78-11e3-81ab-4c9367dc0958";
    private static final String PAYLOAD = """
            {"action":"opened","issue":{"number":1},"repository":{"full_name":"owner/my-repo"}}
            """.strip();

    @TempDir
    Path tempDir;

    private FileBasedTasksConfig config;
    private TaskRepository repository;
    private DynamoDbSource.DeleteTracker deleteTracker;

    @BeforeEach
    void setUp() throws IOException {
        config = FileBasedTasksConfig.create(tempDir);
        repository = FileSystemTaskRepository.create(config);
        Files.createDirectories(config.pendingDir());
        deleteTracker = null;
    }

    private Downloader downloaderWith(List<WebhookRecord> records) {
        DynamoDbSource source = DynamoDbSource.createNull(records);
        deleteTracker = source.trackDeletes();
        return new Downloader(source, repository);
    }

    // 5.1 — empty source: no files written, no errors
    @Test
    void download_withEmptySource_writesNoFiles() {
        Downloader downloader = downloaderWith(List.of());

        downloader.download();

        assertThat(repository.listPending()).isEmpty();
    }

    // 5.2 — one valid record: file created in pending/ with correct name and content
    @Test
    void download_withOneRecord_createsFileInPending() {
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(PAYLOAD))));

        downloader.download();

        List<String> pending = repository.listPending();
        assertThat(pending).hasSize(1);
        String filename = pending.getFirst();
        assertThat(filename).endsWith("_owner-my-repo_72d3162e.json");
    }

    @Test
    void download_withOneRecord_fileContentMatchesPayload() throws IOException {
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(PAYLOAD))));

        downloader.download();

        String filename = repository.listPending().getFirst();
        String content = Files.readString(config.pendingDir().resolve(filename));
        assertThat(content).isEqualTo(PAYLOAD);
    }

    // 5.3 — slash in repo name: filename sanitised correctly
    @Test
    void download_withSlashInRepoName_sanitisesFilename() {
        String payload = """
                {"repository":{"full_name":"acme-corp/backend-api"}}
                """.strip();
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(payload))));

        downloader.download();

        String filename = repository.listPending().getFirst();
        assertThat(filename).contains("acme-corp-backend-api");
        assertThat(filename).doesNotContain("/");
    }

    // 5.4 — write succeeds: DynamoDB record deleted
    @Test
    void download_afterSuccessfulWrite_deletesDynamoDbRecord() {
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(PAYLOAD))));

        downloader.download();

        assertThat(deleteTracker.deletedIds()).containsExactly(DELIVERY_ID);
    }

    // 5.5 — duplicate record: existing file NOT overwritten, DynamoDB record still deleted
    @Test
    void download_whenFileAlreadyInPending_doesNotOverwriteAndStillDeletes() throws IOException {
        // First run: write the file normally
        Downloader firstRun = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(PAYLOAD))));
        firstRun.download();
        String filename = repository.listPending().getFirst();

        // Overwrite with sentinel content to detect whether it gets replaced
        Files.writeString(config.pendingDir().resolve(filename), "original content");

        // Second run — same deliveryId, so same uniqueId suffix "_72d3162e.json"
        Downloader secondRun = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(PAYLOAD))));
        secondRun.download();

        // Only the original file remains — no second file created
        assertThat(repository.listPending()).containsExactly(filename);
        // Original file not overwritten
        assertThat(Files.readString(config.pendingDir().resolve(filename))).isEqualTo("original content");
        // DynamoDB record still deleted (dedup cleanup)
        assertThat(deleteTracker.deletedIds()).containsExactly(DELIVERY_ID);
    }

    // 5.6 — missing repository field: no file written, no DynamoDB delete
    @Test
    void download_withMissingRepositoryField_writesNoFileAndDoesNotDelete() {
        String payloadWithoutRepo = "{\"action\":\"opened\",\"issue\":{\"number\":1}}";
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(payloadWithoutRepo))));

        downloader.download();

        assertThat(repository.listPending()).isEmpty();
        assertThat(deleteTracker.deletedIds()).isEmpty();
    }

    // 5.7 — malformed JSON: no file written, no DynamoDB delete
    @Test
    void download_withMalformedJson_writesNoFileAndDoesNotDelete() {
        String badPayload = "not-valid-json{{{";
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, new WebhookPayload(badPayload))));

        downloader.download();

        assertThat(repository.listPending()).isEmpty();
        assertThat(deleteTracker.deletedIds()).isEmpty();
    }

    // null payload attribute
    @Test
    void download_withNullPayload_writesNoFileAndDoesNotDelete() {
        Downloader downloader = downloaderWith(List.of(new WebhookRecord(DELIVERY_ID, null)));

        downloader.download();

        assertThat(repository.listPending()).isEmpty();
        assertThat(deleteTracker.deletedIds()).isEmpty();
    }
}
