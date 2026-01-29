package com.gemini.webhooks.router.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookFilenameTest {

    @Test
    void toFilename_shouldMatchExpectedFormat() {
        var timestamp = Instant.parse("2026-01-24T10:15:30.123Z");
        var filename = new WebhookFilename(timestamp, "gemini-cli", "a1b2c3d4");

        assertThat(filename.toFilename())
                .isEqualTo("2026-01-24T10:15:30.123Z_gemini-cli_a1b2c3d4.json");
    }

    @Test
    void sanitizedRepoName_shouldReplaceSlashesWithDashes() {
        var filename = new WebhookFilename(Instant.now(), "owner/repo-name", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("owner-repo-name");
    }

    @Test
    void sanitizedRepoName_shouldReplaceMultipleInvalidChars() {
        var filename = new WebhookFilename(Instant.now(), "org/repo.name@v2", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("org-repo-name-v2");
    }

    @Test
    void sanitizedRepoName_shouldPreserveValidChars() {
        var filename = new WebhookFilename(Instant.now(), "my-valid-repo123", "abc123");

        assertThat(filename.sanitizedRepoName()).isEqualTo("my-valid-repo123");
    }

    @Test
    void toFilename_shouldUseUtcTimestamp() {
        var timestamp = Instant.parse("2026-01-24T10:15:30.000Z");
        var filename = new WebhookFilename(timestamp, "repo", "id");

        assertThat(filename.toFilename()).startsWith("2026-01-24T10:15:30.000Z");
    }

    @Test
    void toFilename_shouldEndWithJsonExtension() {
        var filename = new WebhookFilename(Instant.now(), "repo", "id");

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

        assertThat(parsed.timestamp()).isEqualTo(Instant.parse("2026-01-24T12:00:00.000Z"));
    }

    @Test
    void parse_shouldExtractRepoName() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(parsed.repoName()).isEqualTo("my-project");
    }

    @Test
    void parse_shouldExtractUniqueId() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json");

        assertThat(parsed.uniqueId()).isEqualTo("a1b2c3d4");
    }

    @Test
    void parse_shouldHandleRepoNameWithDashes() {
        var parsed = WebhookFilename.parse("2026-01-24T12:00:00.000Z_owner-repo-name_abcd1234.json");

        assertThat(parsed.repoName()).isEqualTo("owner-repo-name");
    }

    @Test
    void parse_shouldRejectMissingJsonExtension() {
        assertThatThrownBy(() -> WebhookFilename.parse("2026-01-24T12:00:00.000Z_repo_a1b2c3d4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match expected pattern");
    }

    @Test
    void parse_shouldRejectWrongSegmentCount() {
        assertThatThrownBy(() -> WebhookFilename.parse("invalid-format.json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match expected pattern");
    }

    @Test
    void parse_shouldRejectInvalidTimestampFormat() {
        assertThatThrownBy(() -> WebhookFilename.parse("2026-01-24_repo_a1b2c3d4.json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match expected pattern");
    }

    @Test
    void parse_shouldRejectInvalidUniqueIdLength() {
        assertThatThrownBy(() -> WebhookFilename.parse("2026-01-24T12:00:00.000Z_repo_abc.json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match expected pattern");
    }

    @Test
    void parse_shouldRoundtripWithToFilename() {
        var original = new WebhookFilename(
                Instant.parse("2026-01-24T10:15:30.123Z"),
                "my-repo",
                "a1b2c3d4"
        );

        var parsed = WebhookFilename.parse(original.toFilename());

        assertThat(parsed.timestamp()).isEqualTo(original.timestamp());
        assertThat(parsed.repoName()).isEqualTo(original.repoName());
        assertThat(parsed.uniqueId()).isEqualTo(original.uniqueId());
    }
}
