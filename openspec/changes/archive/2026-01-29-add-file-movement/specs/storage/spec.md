## ADDED Requirements

### Requirement: File Movement
The system MUST be able to move a file from one storage directory to another atomically.

#### Scenario: Move Pending to Processing
Given a file "2026-01-24T12:00:00.000Z_my-project_abc123.json" exists in `pending/`
When the `move` operation is called with source="pending" and destination="processing"
Then the file exists in `processing/`
And the file no longer exists in `pending/`
And the filename is unchanged.

#### Scenario: Move Processing to Completed
Given a file exists in `processing/`
When the `move` operation is called with source="processing" and destination="completed"
Then the file exists in `completed/`
And the file no longer exists in `processing/`.

#### Scenario: Move Processing to Failed
Given a file exists in `processing/`
When the `move` operation is called with source="processing" and destination="failed"
Then the file exists in `failed/`
And the file no longer exists in `processing/`.

#### Scenario: Source File Not Found
Given no file with the specified name exists in the source directory
When the `move` operation is called
Then an error is raised indicating the source file was not found.
