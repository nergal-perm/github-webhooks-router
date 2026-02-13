# Change: Add CLI argument parsing via picocli

## Why
`FileBasedTasksConfig` hard-codes `storageRoot`, `repoBaseDir`, and `tableName` so the daemon cannot be reconfigured at runtime without recompilation. Operators running the daemon under `systemd` with non-default paths (e.g. `/var/lib/webhooks-router`) have no way to supply configuration without editing source code.

## What Changes
- Add `picocli` dependency to `pom.xml`
- Introduce a `CliArgs` class (picocli `@Command`) that declares `--storage-root`, `--repo-base-dir`, and `--table-name` as optional flags with the existing values as defaults
- `Main.main(String[] args)` parses `args` via picocli and passes the resolved values into `FileBasedTasksConfig`
- `FileBasedTasksConfig` gains a factory `create(CliArgs)` (or equivalent) so wiring stays in `Main` and config stays a plain record
- `--help` is handled by picocli automatically: it prints usage text and exits with code 0

## Impact
- Affected specs: `system-lifecycle` (daemon now accepts CLI flags at startup), new `configuration` capability
- Affected code: `pom.xml`, `Main.java`, `FileBasedTasksConfig.java` (new factory), new `CliArgs.java`
