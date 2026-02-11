package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookFilenameTest {

    @Test
    void toFilename_shouldMatchExpectedFormat() {
        var timestamp = Instant.parse("2026-01-24T10:15:30.123Z");
        var filename = WebhookFilename.create(timestamp, "gemini-cli", "a1b2c3d4");

        assertThat(filename.toFilename())
                .isEqualTo("2026-01-24T10:15:30.123Z_gemini-cli_a1b2c3d4.json");
    }

    @Test
    void sanitizedRepoName_shouldReplaceSlashesWithDashes() {
        var filename = WebhookFilename.create(Instant.now(), "owner/repo-name", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("owner-repo-name");
    }

    @Test
    void sanitizedRepoName_shouldReplaceMultipleInvalidChars() {
        var filename = WebhookFilename.create(Instant.now(), "org/repo.name@v2", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("org-repo-name-v2");
    }

    @Test
    void sanitizedRepoName_shouldPreserveValidChars() {
        var filename = WebhookFilename.create(Instant.now(), "my-valid-repo123", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("my-valid-repo123");
    }

    @Test
    void toFilename_shouldUseUtcTimestamp() {
        var timestamp = Instant.parse("2026-01-24T10:15:30.000Z");
        var filename = WebhookFilename.create(timestamp, "repo", "id");

        assertThat(filename.toFilename()).startsWith("2026-01-24T10:15:30.000Z");
    }

    @Test
    void toFilename_shouldEndWithJsonExtension() {
        var filename = WebhookFilename.create(Instant.now(), "repo", "id");

        assertThat(filename.toFilename()).endsWith(".json");
    }

    @Test
    void create_shouldGenerateFilenameWithCurrentTime() {
        var before = Instant.now();
        var filename = WebhookFilename.create("my-repo");
        var after = Instant.now();

        assertThat(filename.timestamp())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void create_shouldGenerateEightCharUniqueId() {
        var filename = WebhookFilename.create("repo");

        assertThat(filename.uniqueId()).hasSize(8);
    }

    @Test
    void create_shouldGenerateUniqueIds() {
        var filename1 = WebhookFilename.create("repo");
        var filename2 = WebhookFilename.create("repo");

        assertThat(filename1.uniqueId()).isNotEqualTo(filename2.uniqueId());
    }

    @Test
    void parse_shouldExtractTimestamp() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(parsed.toString()).contains(Instant.parse("2026-01-24T12:00:00.000Z").toString());
    }

    @Test
    void parse_shouldExtractRepoName() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(parsed.repoName()).isEqualTo("my-project");
    }

    @Test
    void parse_shouldExtractUniqueId() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(parsed.toString()).contains("a1b2c3d4");
    }

    @Test
    void parse_shouldHandleRepoNameWithDashes() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_owner-repo-name_abcd1234.json");

        assertThat(parsed.repoName()).isEqualTo("owner-repo-name");
    }

    @Test
    void parse_shouldRejectMissingJsonExtension() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_repo_a1b2c3d4");
        assertThat(parsed).isInstanceOf(InvalidAgentTask.class);
    }

    @Test
    void parse_shouldRejectWrongSegmentCount() {
        var parsed = WebhookFilename.parse("invalid-format.json");
        assertThat(parsed).isInstanceOf(InvalidAgentTask.class);
    }

    @Test
    void parse_shouldRejectInvalidTimestampFormat() {
        var parsed  =  WebhookFilename.parse("2026-01-24_repo_a1b2c3d4.json");
        assertThat(parsed).isInstanceOf(InvalidAgentTask.class);
    }

    @Test
    void parse_shouldRejectInvalidUniqueIdLength() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_repo_abc.json");
        assertThat(parsed).isInstanceOf(InvalidAgentTask.class);
    }

    @Test
    void parse_validFilename_isValid() {
        AgentTask task = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(task.isValid()).isTrue();
    }

    @Test
    void parse_invalidFilename_isNotValid() {
        AgentTask task = WebhookFilename.parse("invalid-format.json");

        assertThat(task.isValid()).isFalse();
    }

    @Test
    void parse_invalidFilename_toFilenameReturnsOriginalFilename() {
        AgentTask task = WebhookFilename.parse("invalid-format.json");

        assertThat(task.toFilename()).isEqualTo("invalid-format.json");
    }

    @Test
    void parse_shouldRoundtripWithToFilename() {
        var original = WebhookFilename.create(
                Instant.parse("2026-01-24T10:15:30.123Z"),
                "my-repo",
                "a1b2c3d4"
        );

        var parsed = WebhookFilename.parse(original.toFilename());

        assertThat(parsed.toString()).contains(original.timestamp().toString());
        assertThat(parsed.repoName()).isEqualTo(original.repoName());
        assertThat(parsed.toString()).contains(original.uniqueId());
    }
}
