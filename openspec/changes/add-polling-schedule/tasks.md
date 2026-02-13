## 1. CLI Flags
- [ ] 1.1 Add `--poll-interval-seconds` flag (integer, default 60) to the CLI argument parser
- [ ] 1.2 Add `--quiet-hours-start` flag (HH:MM string, optional) to the CLI argument parser
- [ ] 1.3 Add `--quiet-hours-end` flag (HH:MM string, optional) to the CLI argument parser
- [ ] 1.4 Validate that both `--quiet-hours-start` and `--quiet-hours-end` are supplied together or not at all; log a warning and ignore quiet hours if only one is present

## 2. Polling Schedule Configuration
- [ ] 2.1 Wire the `--poll-interval-seconds` value into the `scheduleAtFixedRate` call in `Main.java` (replacing the hardcoded 60)
- [ ] 2.2 Pass quiet-hours start and end times into `Downloader` (or a `PollingSchedule` value object)

## 3. Quiet Hours Guard in Downloader
- [ ] 3.1 Implement a `QuietHours` value object (or equivalent) that accepts optional start/end `LocalTime` values and exposes `isActive(LocalTime now)`
- [ ] 3.2 Handle midnight-spanning windows (e.g., 22:00–07:00) correctly in `isActive`
- [ ] 3.3 In `Downloader.download()`, check `QuietHours.isActive(LocalTime.now())` before scanning DynamoDB; return early with a debug log if active

## 4. Tests
- [ ] 4.1 Unit-test `QuietHours.isActive` for: time inside window, time outside window, boundary times, midnight-spanning window
- [ ] 4.2 Integration/sociable test for `Downloader`: verify `download()` does NOT call DynamoDB when quiet hours are active
- [ ] 4.3 Integration/sociable test for `Downloader`: verify `download()` proceeds normally when quiet hours are inactive or not configured
