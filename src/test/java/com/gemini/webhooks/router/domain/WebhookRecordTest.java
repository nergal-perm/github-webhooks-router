package com.gemini.webhooks.router.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookRecordTest {

    @Test
    void storesDeliveryIdAndPayload() {
        WebhookPayload payload = new WebhookPayload("{\"action\":\"opened\"}");
        WebhookRecord record = new WebhookRecord("72d3162e-cc78-11e3-81ab-4c9367dc0958", payload);

        assertThat(record.deliveryId()).isEqualTo("72d3162e-cc78-11e3-81ab-4c9367dc0958");
        assertThat(record.payload()).isSameAs(payload);
    }

    @Test
    void allowsNullPayload() {
        WebhookRecord record = new WebhookRecord("delivery-id", null);

        assertThat(record.payload()).isNull();
    }

    @Test
    void uniqueId_stripsDashesAndReturnsFirst8HexChars() {
        WebhookRecord record = new WebhookRecord("72d3162e-cc78-11e3-81ab-4c9367dc0958", null);

        assertThat(record.uniqueId()).isEqualTo("72d3162e");
    }

    @Test
    void repoFullName_returnsEmptyWhenPayloadIsNull() {
        WebhookRecord record = new WebhookRecord("any-id", null);

        assertThat(record.repoFullName()).isEmpty();
    }

    @Test
    void repoFullName_returnsEmptyWhenRepositoryFieldIsMissing() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("{\"action\":\"opened\"}"));

        assertThat(record.repoFullName()).isEmpty();
    }

    @Test
    void repoFullName_returnsEmptyWhenFullNameIsMissing() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("{\"repository\":{\"name\":\"my-repo\"}}"));

        assertThat(record.repoFullName()).isEmpty();
    }

    @Test
    void repoFullName_returnsEmptyWhenFullNameIsJsonNull() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("{\"repository\":{\"full_name\":null}}"));

        assertThat(record.repoFullName()).isEmpty();
    }

    @Test
    void repoFullName_returnsEmptyWhenPayloadIsMalformedJson() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("not-valid-json{{{"));

        assertThat(record.repoFullName()).isEmpty();
    }

    @Test
    void repoFullName_returnsFullNameFromValidPayload() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("""
                {"repository":{"full_name":"owner/my-repo"}}"""));

        assertThat(record.repoFullName()).contains("owner/my-repo");
    }

    @Test
    void rawJson_returnsJsonFromPayload() {
        WebhookRecord record = new WebhookRecord("any-id", new WebhookPayload("{\"action\":\"opened\"}"));

        assertThat(record.rawJson()).isEqualTo("{\"action\":\"opened\"}");
    }

    @Test
    void rawJson_returnsNullWhenPayloadIsNull() {
        WebhookRecord record = new WebhookRecord("any-id", null);

        assertThat(record.rawJson()).isNull();
    }
}
