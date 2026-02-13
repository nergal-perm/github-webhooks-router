package com.gemini.webhooks.router;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CliArgsTest {

    private static CliArgs parse(String... args) {
        CliArgs cliArgs = new CliArgs();
        new CommandLine(cliArgs).parseArgs(args);
        return cliArgs;
    }

    @Test
    void noArgs_usesAllDefaults() {
        FileBasedTasksConfig config = parse().toConfig();

        assertThat(config.storageRoot()).isEqualTo(Path.of("data"));
        assertThat(config.repoBaseDir()).isEqualTo(Path.of(System.getProperty("user.home"), "Dev"));
        assertThat(config.tableName()).isEqualTo("GithubWebhookTable");
    }

    @Test
    void storageRootFlag_overridesStorageRootOnly() {
        FileBasedTasksConfig config = parse("--storage-root", "/var/lib/webhooks-router").toConfig();

        assertThat(config.storageRoot()).isEqualTo(Path.of("/var/lib/webhooks-router"));
        assertThat(config.repoBaseDir()).isEqualTo(Path.of(System.getProperty("user.home"), "Dev"));
        assertThat(config.tableName()).isEqualTo("GithubWebhookTable");
    }

    @Test
    void repoBaseDirFlag_overridesRepoBaseDirOnly() {
        FileBasedTasksConfig config = parse("--repo-base-dir", "/srv/repos").toConfig();

        assertThat(config.storageRoot()).isEqualTo(Path.of("data"));
        assertThat(config.repoBaseDir()).isEqualTo(Path.of("/srv/repos"));
        assertThat(config.tableName()).isEqualTo("GithubWebhookTable");
    }

    @Test
    void tableNameFlag_overridesTableNameOnly() {
        FileBasedTasksConfig config = parse("--table-name", "MyCustomTable").toConfig();

        assertThat(config.storageRoot()).isEqualTo(Path.of("data"));
        assertThat(config.repoBaseDir()).isEqualTo(Path.of(System.getProperty("user.home"), "Dev"));
        assertThat(config.tableName()).isEqualTo("MyCustomTable");
    }

    @Test
    void allFlags_overrideAllDefaults() {
        FileBasedTasksConfig config = parse(
                "--storage-root", "/data",
                "--repo-base-dir", "/repos",
                "--table-name", "ProdTable"
        ).toConfig();

        assertThat(config.storageRoot()).isEqualTo(Path.of("/data"));
        assertThat(config.repoBaseDir()).isEqualTo(Path.of("/repos"));
        assertThat(config.tableName()).isEqualTo("ProdTable");
    }

    @Test
    void helpFlag_requestsUsageHelp() {
        CliArgs cliArgs = new CliArgs();
        CommandLine commandLine = new CommandLine(cliArgs);
        commandLine.parseArgs("--help");

        assertThat(commandLine.isUsageHelpRequested()).isTrue();
    }
}
