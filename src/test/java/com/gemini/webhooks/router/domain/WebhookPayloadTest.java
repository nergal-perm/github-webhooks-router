package com.gemini.webhooks.router.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookPayloadTest {

    @Test
    void issueNumber_withIssueField_returnsIssueNumber() {
        WebhookPayload payload = new WebhookPayload("""
                {"issue": {"number": 42, "title": "Fix the bug"}}
                """);

        assertThat(payload.issueNumber()).isPresent().contains(42);
    }

    @Test
    void issueNumber_withPullRequestField_returnsPRNumber() {
        WebhookPayload payload = new WebhookPayload("""
                {"pull_request": {"number": 123, "title": "Add feature"}}
                """);

        assertThat(payload.issueNumber()).isPresent().contains(123);
    }

    @Test
    void issueNumber_withoutIssueOrPR_returnsEmpty() {
        WebhookPayload payload = new WebhookPayload("""
                {"ref": "refs/heads/main", "commits": []}
                """);

        assertThat(payload.issueNumber()).isEmpty();
    }

    @Test
    void issueNumber_withInvalidJson_returnsEmpty() {
        WebhookPayload payload = new WebhookPayload("{ invalid json }");

        assertThat(payload.issueNumber()).isEmpty();
    }

    @Test
    void issueNumber_withEmptyJson_returnsEmpty() {
        WebhookPayload payload = new WebhookPayload("{}");

        assertThat(payload.issueNumber()).isEmpty();
    }

    @Test
    void eventType_issuesOpened_returnsIssuesOpened() {
        WebhookPayload payload = new WebhookPayload("""
                {"action": "opened", "issue": {"number": 1, "title": "New issue"}}
                """);

        assertThat(payload.eventType()).isEqualTo("issues.opened");
    }

    @Test
    void eventType_issuesClosed_returnsIssuesClosed() {
        WebhookPayload payload = new WebhookPayload("""
                {"action": "closed", "issue": {"number": 1, "title": "Closed issue"}}
                """);

        assertThat(payload.eventType()).isEqualTo("issues.closed");
    }

    @Test
    void eventType_pullRequestOpened_returnsPullRequestOpened() {
        WebhookPayload payload = new WebhookPayload("""
                {"action": "opened", "pull_request": {"number": 5, "title": "New PR"}}
                """);

        assertThat(payload.eventType()).isEqualTo("pull_request.opened");
    }

    @Test
    void eventType_pushEvent_returnsPush() {
        WebhookPayload payload = new WebhookPayload("""
                {"ref": "refs/heads/main", "commits": []}
                """);

        assertThat(payload.eventType()).isEqualTo("push");
    }

    @Test
    void eventType_emptyJson_returnsUnknown() {
        WebhookPayload payload = new WebhookPayload("{}");

        assertThat(payload.eventType()).isEqualTo("unknown");
    }

    @Test
    void eventType_malformedJson_returnsUnknown() {
        WebhookPayload payload = new WebhookPayload("{ not valid json }");

        assertThat(payload.eventType()).isEqualTo("unknown");
    }

    @Test
    void isDispatchable_issuesOpened_returnsTrue() {
        WebhookPayload payload = new WebhookPayload("""
                {"action": "opened", "issue": {"number": 1}}
                """);

        assertThat(payload.isDispatchable()).isTrue();
    }

    @Test
    void isDispatchable_pushEvent_returnsFalse() {
        WebhookPayload payload = new WebhookPayload("""
                {"ref": "refs/heads/main", "commits": []}
                """);

        assertThat(payload.isDispatchable()).isFalse();
    }

    @Test
    void isDispatchable_issuesClosed_returnsFalse() {
        WebhookPayload payload = new WebhookPayload("""
                {"action": "closed", "issue": {"number": 1}}
                """);

        assertThat(payload.isDispatchable()).isFalse();
    }

    @Test
    void rawJson_returnsOriginalString() {
        String json = "{\"action\": \"opened\", \"issue\": {\"number\": 1}}";
        WebhookPayload payload = new WebhookPayload(json);

        assertThat(payload.rawJson()).isEqualTo(json);
    }
}
