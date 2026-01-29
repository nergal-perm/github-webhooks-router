## ADDED Requirements

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
