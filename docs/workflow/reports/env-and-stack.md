# env-and-stack report

## Tech stack confirmation
- Frontend: Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios + Element Plus
- Backend: Java 17 + Spring Boot 3.5.13 + Maven Wrapper
- Runtime mode: MockProvider
- RAG: reserved local knowledge base slot only
- MCP: reserved tool extension slot only

## Environment check
- `node -v`: v22.13.0
- `npm -v`: 11.0.0
- `java -version`: 17.0.8
- `JAVA_HOME`: C:\Program Files\Java\jdk1.8.0_351
- `mvnw.cmd -v`: Maven Wrapper runs on Java 1.8.0_351
- Global Maven: not installed
- Session-only override used for backend verification:
  - `JAVA_HOME=D:\Java`
  - `PATH` prefixed with `D:\Java\bin`
  - `mvnw.cmd -v`: Maven Wrapper runs on Java 17.0.8 from `D:\Java`

## Initialization notes
- Frontend and backend were both empty before initialization.
- Spring Initializr metadata showed `3.5.13.RELEASE` as the latest stable 3.5.x label and `3.5.14.BUILD-SNAPSHOT` as a snapshot option.
- Maven Central resolves the stable backend parent as `3.5.13`, so the project uses `Spring Boot 3.5.13` for stability.

## Verification summary
- Frontend build: passed via `npm run build`
- Frontend dev startup: passed, `http://127.0.0.1:5173` returned HTTP 200
- Backend startup: passed after applying the session-only Java override to `D:\Java`

## Backend environment note
Without a session override, `mvnw.cmd` uses `JAVA_HOME`, which currently points to JDK 8.
Spring Boot 3.5.13 artifacts require Java 17, so backend validation must run with a Java 17 session override until the machine-level Java environment is aligned.
This phase used a session-only override and did not modify any system environment variables.
