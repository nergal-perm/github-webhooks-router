# Implementation Tasks

## 1. Core Dispatcher Logic
- [x] 1.1 Create `Dispatcher` class with scheduled execution capability (60-second interval)
- [x] 1.2 Implement early return check if agent subprocess is currently active
- [x] 1.3 Implement pending directory scanning logic
- [x] 1.4 Extract repository name from webhook filename using existing `WebhookFilename` parser
- [x] 1.5 Read webhook file content as UTF-8 string
- [x] 1.6 Implement file state transition logic (pending → processing)
- [x] 1.7 Add basic error handling and logging

## 2. Agent Subprocess Management
- [x] 2.1 Create `AgentProcess` class for subprocess lifecycle management
- [x] 2.2 Implement working directory change to `~/Dev/<repo-name>`
- [x] 2.3 Implement subprocess launch with command `gemini -y "<webhook-content>"`
- [x] 2.4 Implement subprocess exit code handling
- [x] 2.5 Handle subprocess timeouts (if applicable)

## 3. File State Management
- [x] 3.1 Move processed files to `completed` on success (exit code 0)
- [x] 3.2 Move failed files to `failed` on error (non-zero exit code)
- [x] 3.3 Ensure atomic file operations using existing TaskRepository

## 4. Integration
- [x] 4.1 Update `AppConfig` to include repository base directory (default: `~/Dev`)
- [x] 4.2 Integrate dispatcher into `Main.java` lifecycle with 60-second schedule
- [x] 4.3 Add graceful shutdown for dispatcher thread
- [x] 4.4 Ensure directories are initialized before dispatcher starts

## 5. Testing
- [x] 5.1 Write unit tests for `Dispatcher` class
- [x] 5.2 Write unit tests for `AgentProcess` class
- [x] 5.3 Write integration test for end-to-end webhook processing
- [x] 5.4 Test graceful shutdown behavior

## 6. Documentation
- [x] 6.1 Update README with dispatcher configuration details
- [x] 6.2 Document agent subprocess interface expectations
