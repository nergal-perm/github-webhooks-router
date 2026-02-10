package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebhookFilename implements AgentTask {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9-]");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)_(.+)_([a-f0-9]{8})\\.json$"
    );

    private final Instant timestamp;
    private final String repoName;
    private final String uniqueId;

    private WebhookFilename(Instant timestamp, String repoName, String uniqueId) {
        this.timestamp = timestamp;
        this.repoName = repoName;
        this.uniqueId = uniqueId;
    }

    public static WebhookFilename create(String repoName) {
        return WebhookFilename.create(Instant.now(), repoName, UUID.randomUUID().toString().substring(0, 8));
    }

    public static WebhookFilename create(Instant timestamp, String repoName, String uniqueId) {
        return new WebhookFilename(timestamp, repoName, uniqueId);
    }

    public static AgentTask parse(String filename) {
        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            return new InvalidAgentTask("Filename does not match expected pattern: " + filename);
        }

        String timestampStr = matcher.group(1);
        String repoName = matcher.group(2);
        String uniqueId = matcher.group(3);

        Instant timestamp;
        try {
            timestamp = TIMESTAMP_FORMAT.parse(timestampStr, Instant::from);
        } catch (DateTimeParseException e) {
            return new InvalidAgentTask("Invalid timestamp format in filename: " + filename);
        }

        return new WebhookFilename(timestamp, repoName, uniqueId);
    }

    public String sanitizedRepoName() {
        return INVALID_CHARS.matcher(repoName).replaceAll("-");
    }

    @Override
    public String toFilename() {
        return "%s_%s_%s.json".formatted(
                TIMESTAMP_FORMAT.format(timestamp),
                sanitizedRepoName(),
                uniqueId
        );
    }

    public Instant timestamp() {return timestamp;}

    public String repoName() {return repoName;}

    public String uniqueId() {return uniqueId;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (WebhookFilename) obj;
        return Objects.equals(this.timestamp, that.timestamp) &&
               Objects.equals(this.repoName, that.repoName) &&
               Objects.equals(this.uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, repoName, uniqueId);
    }

    @Override
    public String toString() {
        return "WebhookFilename[" +
               "timestamp=" + timestamp + ", " +
               "repoName=" + repoName + ", " +
               "uniqueId=" + uniqueId + ']';
    }

}
