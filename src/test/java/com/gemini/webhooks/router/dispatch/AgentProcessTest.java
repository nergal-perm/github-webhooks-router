package com.gemini.webhooks.router.dispatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProcessTest {

    @TempDir
    Path tempDir;

    @Test
    void createNull_returnsConfiguredFailure() {
        AgentProcess agentProcess = AgentProcess.createNull(AgentProcess.ProcessResult.failure("simulated error"));

        AgentProcess.ProcessResult result = agentProcess.execute("any-repo", "any content", tempDir.resolve("output.txt"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("simulated error");
    }

    @Test
    void createNull_doesNotTouchTheFilesystem() {
        AgentProcess agentProcess = AgentProcess.createNull();
        Path nonExistentOutput = Path.of("/nonexistent/dir/output.txt");

        AgentProcess.ProcessResult result = agentProcess.execute("any-repo", "any content", nonExistentOutput);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void trackOutput_capturesExecuteCalls() {
        AgentProcess agentProcess = AgentProcess.createNull();
        var output = agentProcess.trackOutput();
        Path outputFile = tempDir.resolve("output.txt");

        agentProcess.execute("my-repo", "webhook payload", outputFile);

        assertThat(output.data()).hasSize(1);
        assertThat(output.data().getFirst().repoName()).isEqualTo("my-repo");
        assertThat(output.data().getFirst().webhookContent()).isEqualTo("webhook payload");
        assertThat(output.data().getFirst().outputFile()).isEqualTo(outputFile);
    }

    @Test
    void createNull_returnsSuccessByDefault() {
        AgentProcess agentProcess = AgentProcess.createNull();

        AgentProcess.ProcessResult result = agentProcess.execute("any-repo", "any content", tempDir.resolve("output.txt"));

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void create_failsWhenRepositoryDirectoryNotFound() {
        AgentProcess agentProcess = AgentProcess.create(tempDir);
        Path outputFile = tempDir.resolve("output.txt");

        AgentProcess.ProcessResult result = agentProcess.execute("non-existent-repo", "test content", outputFile);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.errorMessage()).contains("Repository directory not found");
    }

}
