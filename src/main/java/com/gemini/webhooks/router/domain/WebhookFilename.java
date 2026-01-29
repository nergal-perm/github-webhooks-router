package com.gemini.webhooks.router.domain;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WebhookFilename(Instant timestamp, String repoName, String uniqueId) {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9-]");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)_(.+)_([a-f0-9]{8})\\.json$"
    );

    public static WebhookFilename create(String repoName) {
        return new WebhookFilename(
                Instant.now(),
                repoName,
                UUID.randomUUID().toString().substring(0, 8)
        );
    }

    public static WebhookFilename parse(String filename) {
        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Filename does not match expected pattern: " + filename
            );
        }

        String timestampStr = matcher.group(1);
        String repoName = matcher.group(2);
        String uniqueId = matcher.group(3);

        Instant timestamp;
        try {
            timestamp = TIMESTAMP_FORMAT.parse(timestampStr, Instant::from);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid timestamp format in filename: " + filename, e
            );
        }

        return new WebhookFilename(timestamp, repoName, uniqueId);
    }

    public String sanitizedRepoName() {
        return INVALID_CHARS.matcher(repoName).replaceAll("-");
    }

    public String toFilename() {
        return "%s_%s_%s.json".formatted(
                TIMESTAMP_FORMAT.format(timestamp),
                sanitizedRepoName(),
                uniqueId
        );
    }
}
