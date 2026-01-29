# Storage Specification

## Purpose
Defines the behavior for the local file-based persistence layer, which serves as the durable buffer between the cloud source and local processing agents.
## Requirements
### Requirement: Directory Initialization
The system MUST ensure its required workspace exists before performing any operations.

#### Scenario: Startup Check
Given the application starts with storage root `/var/lib/webhooks`
When the directory does not exist
Then the application creates `/var/lib/webhooks/pending`.

### Requirement: Webhook Persistence
The system MUST be able to save a text payload to the pending queue.

#### Scenario: Valid Save
Given a repository name "gemini-cli" and a JSON payload "{}"
When the `save` operation is called
Then a new file is created in the `pending` directory
And the file content matches the JSON payload exactly.

### Requirement: File Naming Convention
Stored files MUST adhere to a strict naming pattern to support metadata extraction without parsing content.

#### Format
`{ISO-8601-Timestamp}_{Sanitized-RepoName}_{UniqueId}.json`

#### Scenario: Filename Structure
Given a save request for repo "my-project" at time "2026-01-24T12:00:00Z"
When the file is saved
Then the filename starts with "2026-01-24T12:00:00"
And contains "my-project"
And ends with ".json".

#### Scenario: Input Sanitization
Given a repository name "owner/repo-name" (contains slashes)
When the file is saved
Then the filename contains "owner-repo-name" (slashes replaced or removed)
And does NOT contain actual subdirectories.

#### Scenario: Filename Parsing
Given a filename "2026-01-24T12:00:00.000Z_my-project_a1b2c3d4.json"
When the filename is parsed
Then the extracted timestamp is "2026-01-24T12:00:00.000Z"
And the extracted repository name is "my-project"
And the extracted unique ID is "a1b2c3d4".

#### Scenario: Malformed Filename Rejection
Given a filename "invalid-format.json"
When parsing is attempted
Then an error is raised indicating the filename does not match the expected pattern.

### Requirement: Directory Listing
The system MUST be able to list all files in a storage directory.

#### Scenario: List Pending Files
Given the `pending` directory contains files "a.json" and "b.json"
When the `list` operation is called
Then the result contains "a.json" and "b.json".

#### Scenario: Empty Directory
Given the `pending` directory is empty
When the `list` operation is called
Then the result is an empty list.

#### Scenario: Returns Filenames Only
Given the `pending` directory contains a file
When the `list` operation is called
Then the result contains only the filename, not the full path.

