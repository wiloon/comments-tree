# AGENTS.md — AI Coding Guidelines

This file provides instructions for AI agents (GitHub Copilot, OpenAI Codex, etc.) working in this repository.

---

## Project Overview

**comments-tree** is a threaded comment system with:
- **Backend**: Spring Boot 3.5.14, Spring Security 6.x, SQLite (prod), H2 (test), Jetty, Java 17 target
- **Frontend**: Vue.js 2.x + TypeScript + Vuetify 2.x
- **Build**: Maven 3.x (multi-module), Node.js via fnm
- **Dev runner**: [Task](https://taskfile.dev) (`Taskfile.yml`)

Module layout:
```
comments-tree-api/   # Spring Boot backend
comments-tree-web/   # Vue.js frontend
Taskfile.yml         # dev convenience tasks
```

---

## Language Policy

### Code (Java, TypeScript, Vue)
- **All new or modified code MUST use English** for:
  - Class names, method names, variable names, field names
  - Code comments (`//`, `/* */`, Javadoc `/** */`)
  - Log messages (`logger.info(...)`, `logger.error(...)`)
  - Exception messages
  - Test method names and assertion messages
- Chinese text is only acceptable in end-user facing UI strings (e.g., frontend `<template>` display text) and `application.properties` descriptions if they were already in Chinese.
- **When modifying or creating any file, convert all existing Chinese comments, Javadoc, and log/exception messages in that file to English.**

### Taskfile (`Taskfile.yml`)
- Task `desc` fields and inline comments **must be in English**.
- Do not introduce Chinese in new or modified tasks.

### Configuration files
- `application.properties` keys and values that are developer-facing should use English.
- SQL schema comments and column names must use English.

---

## Java / Spring Boot Conventions

### Dependency Injection
- **Always use constructor injection**. Never use field injection (`@Autowired` on a field).
- Declare injected fields as `private final`.

### Controller
- Use `@RestController` (not `@Controller` + `@ResponseBody` per method).
- Validate request bodies with `@Validated` / `@Valid`.

### Service / DAO
- One responsibility per class; keep `@Service` and `@Repository` layers separate.

### Error Handling
- Do not use `e.printStackTrace()`. Always log with SLF4J: `logger.error("message", e)`.
- Unhandled exceptions are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`).

### Security
- Use `SecurityFilterChain` bean pattern (not `WebSecurityConfigurerAdapter`).
- Password encoding is done through the injected `PasswordEncoder` (provided by `PasswordEncoderConfig`).

### Process Execution
- Always pass commands as `String[]` to `Runtime.getRuntime().exec()` — never concatenate a command string.

---

## Testing Conventions

### Unit tests (`*Test.java`)
- Suffix: `*Test` — picked up by **maven-surefire-plugin** (`mvn test`)
- Runner: `@ExtendWith(MockitoExtension.class)`
- Use `@Mock` + `@InjectMocks`; no Spring context
- No `@SpringBootTest`, no `@MockBean`, no `@Autowired`

### Integration tests (`*IT.java`)
- Suffix: `*IT` — picked up by **maven-failsafe-plugin** (`mvn verify`)
- Runner: `@SpringBootTest` + `@AutoConfigureMockMvc`
- Full Spring context with H2 in-memory DB (configured in `src/test/resources/application.properties`)

### When to use which
| Need                              | Test type                            |
| --------------------------------- | ------------------------------------ |
| Pure business logic, no I/O       | Unit test (`*Test`)                  |
| Repository / SQL logic            | Unit test with mocked `JdbcTemplate` |
| HTTP routing, security, full flow | Integration test (`*IT`)             |

---

## File Structure — Do Not Change

- `PasswordEncoderConfig.java` must remain a separate `@Configuration` class to avoid circular dependency between `UserService` and `SecurityConfig`.
- Test resources live in `comments-tree-api/src/test/resources/` (separate `application.properties` and `schema.sql` for H2).

---

## Commands

```bash
# Unit tests only
mvn test -pl comments-tree-api -am

# Unit + integration tests
mvn verify -pl comments-tree-api -am -DskipFrontend=true

# Start backend
task api

# Start frontend dev server
task ui
```
