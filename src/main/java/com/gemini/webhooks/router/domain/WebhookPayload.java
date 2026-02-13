package com.gemini.webhooks.router.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WebhookPayload {

    private static final Logger logger = LoggerFactory.getLogger(WebhookPayload.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String json;

    public WebhookPayload(String json) {
        this.json = json;
    }

    public Optional<Integer> issueNumber() {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("issue") && root.get("issue").has("number")) {
                return Optional.of(root.get("issue").get("number").asInt());
            }
            if (root.has("pull_request") && root.get("pull_request").has("number")) {
                return Optional.of(root.get("pull_request").get("number").asInt());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse webhook JSON for issue number", e);
            return Optional.empty();
        }
    }

    public String eventType() {
        try {
            JsonNode root = objectMapper.readTree(json);
            String action = root.has("action") ? root.get("action").asText() : null;
            if (root.has("issue") && action != null) {
                return "issues." + action;
            }
            if (root.has("pull_request") && action != null) {
                return "pull_request." + action;
            }
            if (root.has("ref") && root.has("commits")) {
                return "push";
            }
            return "unknown";
        } catch (Exception e) {
            logger.warn("Failed to parse webhook JSON for event type", e);
            return "unknown";
        }
    }

    public boolean isDispatchable() {
        return "issues.opened".equals(eventType());
    }

    public Optional<String> repoFullName() {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode fullName = root.path("repository").path("full_name");
            if (fullName.isMissingNode() || fullName.isNull()) {
                return Optional.empty();
            }
            return Optional.of(fullName.asText());
        } catch (Exception e) {
            logger.warn("Failed to parse webhook JSON for repository name", e);
            return Optional.empty();
        }
    }

    public String rawJson() {
        return json;
    }
}
