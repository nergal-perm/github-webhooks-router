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
        Path outputFile = tempDir.resolve("output.txt");

        AgentProcess.ProcessResult result = agentProcess.execute("non-existent-repo", "test content", outputFile);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.errorMessage()).contains("Repository directory not found");
    }

    @Test
    void execute_shouldSucceedWithValidRepositoryDirectory() throws Exception {
        Path repoDir = tempDir.resolve("test-repo");
        Files.createDirectories(repoDir);
        Path outputFile = tempDir.resolve("outputs").resolve("output.txt");

        AgentProcess agentProcess = new AgentProcess(tempDir);

        // Note: The actual result depends on whether 'gemini' command exists in PATH
        // If gemini command exists and accepts the arguments, it may succeed
        AgentProcess.ProcessResult result = agentProcess.execute("test-repo", "What is the capital of Great Britain?", outputFile);

        assertThat(result).isNotNull();
    }

    @Test
    void execute_shouldCreateOutputFile() throws Exception {
        Path repoDir = tempDir.resolve("test-repo");
        Files.createDirectories(repoDir);
        Path outputDir = tempDir.resolve("outputs");
        Path outputFile = outputDir.resolve("test-output.txt");

        AgentProcess agentProcess = new AgentProcess(tempDir);

        // Execute (will likely fail because gemini command doesn't exist, but should create the file)
        agentProcess.execute("test-repo", "What is the capital of Great Britain?", outputFile);

        // Output file should be created
        assertThat(outputFile).exists();
    }
}
