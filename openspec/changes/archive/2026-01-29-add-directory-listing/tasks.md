# Tasks: Add Directory Listing

## 1. Implementation
- [x] Add `List<String> list()` method to `TaskRepository` interface.
- [x] Implement in `FileSystemRepository` using `Files.list()`.
    - [x] Return filenames only (not full paths).
    - [x] Filter to regular files only (exclude subdirectories).

## 2. Tests
- [x] Integration test: list files in a directory with multiple files.
- [x] Integration test: list returns empty for empty directory.
- [x] Integration test: verify filenames returned (not paths).
