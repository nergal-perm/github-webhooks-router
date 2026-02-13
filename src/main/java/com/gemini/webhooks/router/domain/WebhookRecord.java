package com.gemini.webhooks.router.domain;

import java.util.Optional;

public record WebhookRecord(String deliveryId, WebhookPayload payload) {

    public String uniqueId() {
        return deliveryId.replace("-", "").substring(0, 8);
    }

    public Optional<String> repoFullName() {
        if (payload == null) return Optional.empty();
        return payload.repoFullName();
    }

    public String rawJson() {
        if (payload == null) return null;
        return payload.rawJson();
    }
}
