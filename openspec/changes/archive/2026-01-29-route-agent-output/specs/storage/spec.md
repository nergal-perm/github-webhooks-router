# storage Spec Delta

## ADDED Requirements

### Requirement: Output Directory Management
The storage layer MUST provide a dedicated directory for agent session output files.

#### Scenario: Output Directory Initialization
Given the storage root is "/var/lib/webhooks-router"
When the daemon starts
Then the storage layer creates the directory "/var/lib/webhooks-router/outputs" if it does not exist
And sets appropriate permissions for writing output files.

#### Scenario: Output Directory Location
Given the storage root is configured
When the application queries the outputs directory path
Then it returns "{storageRoot}/outputs".
