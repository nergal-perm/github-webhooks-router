package com.gemini.webhooks.router.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AgentProcess {
    private static final Logger logger = LoggerFactory.getLogger(AgentProcess.class);
    private static final String AGENT_COMMAND = "gemini";

    private final Path repoBaseDir;

    public AgentProcess(Path repoBaseDir) {
        this.repoBaseDir = repoBaseDir;
    }

    public ProcessResult execute(String repoName, String webhookContent) {
        Path repoDir = repoBaseDir.resolve(repoName);

        if (!Files.isDirectory(repoDir)) {
            logger.error("Repository directory not found: {}", repoDir);
            return ProcessResult.failure("Repository directory not found: " + repoDir);
        }

        try {
            logger.info("Launching agent for repository: {} in directory: {}", repoName, repoDir);
            Process process = new ProcessBuilder(AGENT_COMMAND, "-y", webhookContent)
                    .directory(repoDir.toFile())
                    .inheritIO()
                    .start();

            int exitCode = process.waitFor();
            logger.info("Agent process completed with exit code: {}", exitCode);

            if (exitCode == 0) {
                return ProcessResult.success();
            } else {
                return ProcessResult.failure("Agent process exited with code: " + exitCode);
            }
        } catch (IOException e) {
            logger.error("Failed to launch agent process", e);
            return ProcessResult.failure("Failed to launch agent: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Agent process interrupted", e);
            return ProcessResult.failure("Agent process interrupted: " + e.getMessage());
        }
    }

    public record ProcessResult(boolean isSuccess, String errorMessage) {
        public static ProcessResult success() {
            return new ProcessResult(true, null);
        }

        public static ProcessResult failure(String errorMessage) {
            return new ProcessResult(false, errorMessage);
        }
    }
}
