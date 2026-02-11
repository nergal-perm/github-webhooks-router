## ADDED Requirements
### Requirement: Skipped Directory
The storage layer MUST provide a `skipped/` directory for webhook files that were intentionally not processed due to unsupported event types.

#### Scenario: Skipped Directory Initialization
Given the storage root is "/var/lib/webhooks-router"
When the daemon starts
Then the storage layer creates the directory "/var/lib/webhooks-router/skipped" if it does not exist.

#### Scenario: Skipped Directory Location
Given the storage root is configured
When the application queries the skipped directory path
Then it returns "{storageRoot}/skipped".
