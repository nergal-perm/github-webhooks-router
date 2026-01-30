package com.gemini.webhooks.router.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookParserTest {

    @Test
    void extractIssueNumber_withIssueField_returnsIssueNumber() {
        String webhookJson = """
                {
                    "issue": {
                        "number": 42,
                        "title": "Fix the bug"
                    }
                }
                """;

        Optional<Integer> result = WebhookParser.extractIssueNumber(webhookJson);

        assertThat(result).isPresent().contains(42);
    }

    @Test
    void extractIssueNumber_withPullRequestField_returnsPRNumber() {
        String webhookJson = """
                {
                    "pull_request": {
                        "number": 123,
                        "title": "Add feature"
                    }
                }
                """;

        Optional<Integer> result = WebhookParser.extractIssueNumber(webhookJson);

        assertThat(result).isPresent().contains(123);
    }

    @Test
    void extractIssueNumber_withoutIssueOrPR_returnsEmpty() {
        String webhookJson = """
                {
                    "ref": "refs/heads/main",
                    "commits": []
                }
                """;

        Optional<Integer> result = WebhookParser.extractIssueNumber(webhookJson);

        assertThat(result).isEmpty();
    }

    @Test
    void extractIssueNumber_withInvalidJson_returnsEmpty() {
        String webhookJson = "{ invalid json }";

        Optional<Integer> result = WebhookParser.extractIssueNumber(webhookJson);

        assertThat(result).isEmpty();
    }

    @Test
    void extractIssueNumber_withEmptyJson_returnsEmpty() {
        String webhookJson = "{}";

        Optional<Integer> result = WebhookParser.extractIssueNumber(webhookJson);

        assertThat(result).isEmpty();
    }
}
