## ADDED Requirements

### Requirement: CLI Configuration Flags
The daemon MUST accept optional command-line flags to override each built-in default configuration value at startup.

#### Scenario: All defaults when no flags provided
- **WHEN** the daemon is started with no arguments
- **THEN** `storageRoot` resolves to `data`
- **AND** `repoBaseDir` resolves to `${user.home}/Dev`
- **AND** `tableName` resolves to `GithubWebhookTable`

#### Scenario: --storage-root overrides default
- **WHEN** the daemon is started with `--storage-root /var/lib/webhooks-router`
- **THEN** `storageRoot` resolves to `/var/lib/webhooks-router`
- **AND** all other configuration values retain their defaults

#### Scenario: --repo-base-dir overrides default
- **WHEN** the daemon is started with `--repo-base-dir /srv/repos`
- **THEN** `repoBaseDir` resolves to `/srv/repos`
- **AND** all other configuration values retain their defaults

#### Scenario: --table-name overrides default
- **WHEN** the daemon is started with `--table-name MyCustomTable`
- **THEN** `tableName` resolves to `MyCustomTable`
- **AND** all other configuration values retain their defaults

#### Scenario: All flags provided together
- **WHEN** the daemon is started with `--storage-root /data --repo-base-dir /repos --table-name ProdTable`
- **THEN** each configuration value reflects the supplied flag value

### Requirement: Help Flag
The daemon MUST print usage information and exit cleanly when `--help` is passed.

#### Scenario: --help prints usage and exits with code 0
- **WHEN** the daemon is started with `--help`
- **THEN** usage text including all available flags is printed to standard output
- **AND** the process exits with exit code 0
- **AND** no daemon threads are started
