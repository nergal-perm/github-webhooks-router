## 1. Dependency
- [x] 1.1 Add `info.picocli:picocli:4.7.6` to `pom.xml`

## 2. CLI argument class
- [x] 2.1 Create `CliArgs` class annotated with picocli `@Command` (name `webhooks-router`, `mixinStandardHelpOptions = true`)
- [x] 2.2 Declare `--storage-root` option (`Path`, default `data`)
- [x] 2.3 Declare `--repo-base-dir` option (`Path`, default `~/Dev` resolved via `user.home`)
- [x] 2.4 Declare `--table-name` option (`String`, default `GithubWebhookTable`)
- [x] 2.5 Write tests: no args produces all defaults
- [x] 2.6 Write tests: each flag individually overrides its respective default
- [x] 2.7 Write tests: `--help` causes exit code 0 (picocli built-in)

## 3. FileBasedTasksConfig wiring
- [x] 3.1 Add `FileBasedTasksConfig.create(CliArgs args)` factory that constructs from the parsed options
- [x] 3.2 Write tests: factory correctly maps each `CliArgs` field to its `FileBasedTasksConfig` field

## 4. Wire into Main
- [x] 4.1 Replace `FileBasedTasksConfig.create()` in `Main.main` with picocli parse of `args` then `FileBasedTasksConfig.create(cliArgs)`
- [x] 4.2 On picocli parse error, print usage to stderr and exit with non-zero code
