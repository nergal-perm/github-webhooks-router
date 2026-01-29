## MODIFIED Requirements

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
