package com.gemini.webhooks.router.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OutputFilenameTest {

    @Test
    void generate_withIssueNumber_includesIssueInFilename() {
        String repoName = "my-repo";
        Optional<Integer> issueNumber = Optional.of(42);
        Instant timestamp = Instant.parse("2026-01-30T10:30:00.000Z");

        String result = OutputFilename.generate(repoName, issueNumber, timestamp);

        assertThat(result).isEqualTo("my-repo_issue-42_2026-01-30T10:30:00.000Z.txt");
    }

    @Test
    void generate_withoutIssueNumber_omitsIssueFromFilename() {
        String repoName = "my-repo";
        Optional<Integer> issueNumber = Optional.empty();
        Instant timestamp = Instant.parse("2026-01-30T10:30:00.000Z");

        String result = OutputFilename.generate(repoName, issueNumber, timestamp);

        assertThat(result).isEqualTo("my-repo_2026-01-30T10:30:00.000Z.txt");
    }

    @Test
    void generate_withDifferentRepoName_usesCorrectRepoName() {
        String repoName = "another-project";
        Optional<Integer> issueNumber = Optional.of(99);
        Instant timestamp = Instant.parse("2026-01-30T15:45:30.123Z");

        String result = OutputFilename.generate(repoName, issueNumber, timestamp);

        assertThat(result).isEqualTo("another-project_issue-99_2026-01-30T15:45:30.123Z.txt");
    }

    @Test
    void generate_formatsTimestampCorrectly() {
        String repoName = "test-repo";
        Optional<Integer> issueNumber = Optional.empty();
        Instant timestamp = Instant.parse("2026-12-31T23:59:59.999Z");

        String result = OutputFilename.generate(repoName, issueNumber, timestamp);

        assertThat(result).isEqualTo("test-repo_2026-12-31T23:59:59.999Z.txt");
    }
}
