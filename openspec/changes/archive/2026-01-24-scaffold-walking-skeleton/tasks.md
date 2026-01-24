# Implementation Tasks

1.  **Project Initialization**
    - [x] Create `pom.xml` with Java 21 compiler source/target.
    - [x] Create standard directory structure `src/main/java`, `src/main/resources`.

2.  **Dependencies**
    - [x] Add `slf4j-api` and `logback-classic` to `pom.xml`.

3.  **Core Implementation**
    - [x] Implement `com.gemini.webhooks.router.Main` class.
    - [x] Setup `ScheduledExecutorService` for 1-minute interval logging.
    - [x] Add `ShutdownHook` for graceful exit.
    - [x] Configure `logback.xml` for console output (Systemd handles rotation/capture).

4.  **Build Configuration**
    - [x] Configure `maven-shade-plugin` or `maven-assembly-plugin` to produce an executable "fat JAR" (uber-jar).

5.  **Deployment Artifacts**
    - [x] Create `dist/webhooks-router.service` systemd template.

6.  **Verification**
    - [x] Run `mvn clean package`.
    - [x] Execute `java -jar target/webhooks-router-*.jar` and verify logs appear for at least 2 minutes.
