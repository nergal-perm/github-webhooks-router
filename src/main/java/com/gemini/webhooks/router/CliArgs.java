package com.gemini.webhooks.router;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(name = "webhooks-router", mixinStandardHelpOptions = true,
        description = "GitHub Webhooks Router Daemon")
public class CliArgs {

    @Option(names = "--storage-root", description = "Storage root directory (default: data)")
    private Path storageRoot = Path.of("data");

    @Option(names = "--repo-base-dir", description = "Repository base directory (default: ~/Dev)")
    private Path repoBaseDir = Path.of(System.getProperty("user.home"), "Dev");

    @Option(names = "--table-name", description = "DynamoDB table name (default: GithubWebhookTable)")
    private String tableName = "GithubWebhookTable";

    public FileBasedTasksConfig toConfig() {
        return new FileBasedTasksConfig(storageRoot, repoBaseDir, tableName);
    }
}
