# Implementation Tasks

## 1. Configuration
- [x] 1.1 Add `outputsDir()` method to `AppConfig.java` returning `storageRoot/outputs`
- [x] 1.2 Update storage initialization to create `outputs/` directory on startup

## 2. Webhook Parsing
- [x] 2.1 Add method to parse webhook JSON and extract issue number (if present)
- [x] 2.2 Handle webhook payloads without issue numbers gracefully (return null/Optional.empty)

## 3. Output File Generation
- [x] 3.1 Add method to generate output filename with pattern: `{reponame}_{issue}_{timestamp}.txt`
- [x] 3.2 Use ISO 8601 timestamp format for consistency with webhook filenames
- [x] 3.3 Handle optional issue number in filename (omit if not present)

## 4. Subprocess Output Redirection
- [x] 4.1 Update `AgentProcess.execute()` to accept output file path parameter
- [x] 4.2 Generate output file path in `outputs/` directory
- [x] 4.3 Replace `.inheritIO()` with `.redirectOutput()` and `.redirectError()` to the same file
- [x] 4.4 Ensure output file parent directory is created before process starts

## 5. Integration
- [x] 5.1 Update `Dispatcher.processWebhook()` to parse issue number from webhook content
- [x] 5.2 Pass output file path to `AgentProcess.execute()`
- [x] 5.3 Log output file path for traceability

## 6. Testing
- [x] 6.1 Add unit test for issue number extraction from webhook JSON
- [x] 6.2 Add unit test for output filename generation with and without issue numbers
- [x] 6.3 Add integration test verifying agent output is written to file
- [x] 6.4 Updated existing tests to work with new signature
- [x] 6.5 Verified all unit tests pass

## 7. Error Handling
- [x] 7.1 Handle JSON parsing errors gracefully (log and continue without issue number)
- [x] 7.2 Handle file creation errors via IOException in AgentProcess
- [x] 7.3 Ensure output directory exists before attempting to write via Files.createDirectories()
