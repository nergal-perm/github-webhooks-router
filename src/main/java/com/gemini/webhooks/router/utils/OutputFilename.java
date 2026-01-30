package com.gemini.webhooks.router.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OutputFilename {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    /**
     * Generates an output filename for an agent session.
     * Pattern: {reponame}_issue-{number}_{timestamp}.txt or {reponame}_{timestamp}.txt
     *
     * @param repoName Repository name
     * @param issueNumber Optional issue number
     * @param timestamp Session start timestamp
     * @return Generated filename
     */
    public static String generate(String repoName, Optional<Integer> issueNumber, Instant timestamp) {
        String timestampStr = FORMATTER.format(timestamp);

        if (issueNumber.isPresent()) {
            return String.format("%s_issue-%d_%s.txt", repoName, issueNumber.get(), timestampStr);
        } else {
            return String.format("%s_%s.txt", repoName, timestampStr);
        }
    }
}
