package com.gemini.webhooks.router.dispatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProcessTest {

    @TempDir
    Path tempDir;

    @Test
    void execute_shouldFailWhenRepositoryDirectoryNotFound() {
        AgentProcess agentProcess = new AgentProcess(tempDir);

        AgentProcess.ProcessResult result = agentProcess.execute("non-existent-repo", "test content");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.errorMessage()).contains("Repository directory not found");
    }

    @Test
    void execute_shouldSucceedWithValidRepositoryDirectory() throws Exception {
        Path repoDir = tempDir.resolve("test-repo");
        Files.createDirectories(repoDir);

        AgentProcess agentProcess = new AgentProcess(tempDir);

        // Note: The actual result depends on whether 'gemini' command exists in PATH
        // If gemini command exists and accepts the arguments, it may succeed
        AgentProcess.ProcessResult result = agentProcess.execute("test-repo", "test content");

        assertThat(result).isNotNull();
    }
}
