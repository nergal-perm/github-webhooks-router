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

    @Test
    void extractEventType_issuesOpened_returnsIssuesOpened() {
        String webhookJson = """
                {
                    "action": "opened",
                    "issue": {
                        "number": 1,
                        "title": "New issue"
                    }
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isEqualTo("issues.opened");
    }

    @Test
    void extractEventType_issuesClosed_returnsIssuesClosed() {
        String webhookJson = """
                {
                    "action": "closed",
                    "issue": {
                        "number": 1,
                        "title": "Closed issue"
                    }
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isEqualTo("issues.closed");
    }

    @Test
    void extractEventType_pullRequestOpened_returnsPullRequestOpened() {
        String webhookJson = """
                {
                    "action": "opened",
                    "pull_request": {
                        "number": 5,
                        "title": "New PR"
                    }
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isEqualTo("pull_request.opened");
    }

    @Test
    void extractEventType_pushEvent_returnsPush() {
        String webhookJson = """
                {
                    "ref": "refs/heads/main",
                    "commits": []
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isEqualTo("push");
    }

    @Test
    void extractEventType_issueWithoutAction_returnsUnknown() {
        String webhookJson = """
                {
                    "issue": {
                        "number": 1
                    }
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isEqualTo("unknown");
    }

    @Test
    void extractEventType_issueComment_returnsIssuesAction() {
        String webhookJson = """
                {
                    "action": "created",
                    "issue": {
                        "number": 1
                    },
                    "comment": {
                        "body": "A comment"
                    }
                }
                """;

        String result = WebhookParser.extractEventType(webhookJson);

        assertThat(result).isNotEqualTo("issues.opened");
    }

    @Test
    void extractEventType_emptyJson_returnsUnknown() {
        String result = WebhookParser.extractEventType("{}");

        assertThat(result).isEqualTo("unknown");
    }

    @Test
    void extractEventType_malformedJson_returnsUnknown() {
        String result = WebhookParser.extractEventType("{ not valid json }");

        assertThat(result).isEqualTo("unknown");
    }
}
