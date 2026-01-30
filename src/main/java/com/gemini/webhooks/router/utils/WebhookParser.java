package com.gemini.webhooks.router.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WebhookParser {
    private static final Logger logger = LoggerFactory.getLogger(WebhookParser.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts the issue number from a GitHub webhook JSON payload.
     * Checks both "issue" and "pull_request" fields.
     *
     * @param webhookJson The raw webhook JSON content
     * @return Optional containing the issue number if found, empty otherwise
     */
    public static Optional<Integer> extractIssueNumber(String webhookJson) {
        try {
            JsonNode root = objectMapper.readTree(webhookJson);

            // Check for issue.number
            if (root.has("issue") && root.get("issue").has("number")) {
                return Optional.of(root.get("issue").get("number").asInt());
            }

            // Check for pull_request.number
            if (root.has("pull_request") && root.get("pull_request").has("number")) {
                return Optional.of(root.get("pull_request").get("number").asInt());
            }

            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to parse webhook JSON for issue number", e);
            return Optional.empty();
        }
    }
}
