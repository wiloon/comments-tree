---
mode: agent
description: Apply Spring Boot best practices and establish a proper test pyramid for the comments-tree backend.
---

# Task Spec: Spring Boot Best Practices & Test Coverage

## Background

The `comments-tree-api` backend was written in 2022 and contains patterns that are now considered outdated or unsafe:
- `WebSecurityConfigurerAdapter` (deprecated in Spring Security 5.7, removed in 6.0)
- Field injection via `@Autowired` (untestable, hides dependencies)
- `e.printStackTrace()` (not captured by log aggregators)
- `Runtime.exec(String)` with a concatenated command string (security risk)
- No null guards after database lookups
- No global exception handler (unhandled exceptions return HTML to a JSON API)
- No meaningful test coverage; all tests relied on a full Spring context even when unnecessary

The goal is to modernise the code to Spring Boot 2.6 / Spring Security 5.6 best practices and establish a proper unit-vs-integration test pyramid.

> **Implementation status (2026-05-20):** Most requirements are done; **R3** and **R4** have minor gaps. See per-item markers below and [Acceptance Criteria](#acceptance-criteria).

---

## Goals

1. **Code quality** — eliminate anti-patterns; make the code reviewable against modern Spring Boot standards.
2. **Test coverage** — cover `UserService` business logic with fast unit tests; cover HTTP routing and security rules with integration tests; cover the full user journey with an end-to-end integration test.
3. **Test layering** — `mvn test` must run only unit tests; `mvn verify` must run unit + integration tests.
4. **AI guidance** — leave documentation so that future AI agents apply the same conventions automatically.

---

## Requirements

### R1 — Spring Security modernisation ✅

- [x] Remove `extends WebSecurityConfigurerAdapter` from `SecurityConfig`.
- [x] Replace with a `@Bean SecurityFilterChain filterChain(HttpSecurity)` method.
- [x] Replace `@EnableGlobalMethodSecurity` with `@EnableMethodSecurity(prePostEnabled = true)`.
- [x] Expose `AuthenticationManager` as a `@Bean` via `AuthenticationConfiguration`.

### R2 — Circular dependency resolution ✅

- [x] Extract `@Bean PasswordEncoder` into a dedicated `PasswordEncoderConfig` class.
- [x] `UserService` and `SecurityConfig` must each inject `PasswordEncoder` independently.
- [x] Verify: the application starts without `BeanCurrentlyInCreationException`.

### R3 — Constructor injection throughout ⚠️ partial

- [x] All `@Service`, `@Repository`, `@RestController`, and `@Configuration` beans use constructor injection.
- [ ] **Outstanding:** `BrowserCommandRunner` still uses `@Autowired` field injection for `Browser` (should be constructor-injected `private final Browser browser`).

### R4 — Controller conventions ⚠️ partial

- [x] Use `@RestController` (not `@Controller` + per-method `@ResponseBody`).
- [ ] **Outstanding:** `CommentController` uses `@Valid` on POST body; `UserController.register()` does not validate `@RequestBody User` with `@Valid` / `@Validated`.

### R5 — Null safety in `CommentController` ✅

- [x] After `userService.getUserByNameOrEmail(name)`, check for `null` before accessing `user.getId()`.
- [x] Return `CommonResult.failed("Authenticated user not found")` when the user is not found.

### R6 — Logging & exception hygiene ⚠️ partial

- [x] Replace every `e.printStackTrace()` with `logger.error("message", e)` (SLF4J).
- [x] Add `GlobalExceptionHandler` (`@RestControllerAdvice`) that catches `Exception`, logs it, and returns HTTP 500.
- [ ] **Note:** handler message is `"Internal server error"` (English), not the spec's `"服务器内部错误"`.

### R7 — Secure process execution ✅

- [x] Replace `Runtime.getRuntime().exec(command + " " + url)` with `Runtime.getRuntime().exec(new String[]{command, url})`.

### R8 — JSON serialisation consistency ✅

- [x] Replace `org.eclipse.jetty.util.ajax.JSON` (internal Jetty API) with `com.alibaba.fastjson.JSON.toJSONString()` in `RestAuthenticationEntryPoint` and `RestfulAccessDeniedHandler`.

### R9 — Unit tests for `UserService` (no Spring context) ✅

Naming: `UserServiceUnitTest.java` (surefire picks it up via `*Test` pattern).

Runner: `@RunWith(MockitoJUnitRunner.class)` with `@Mock JdbcTemplate`, `@Mock PasswordEncoder`, `@InjectMocks UserService`.

Required test cases:

| Method name | Scenario | Expected outcome |
|---|---|---|
| `getUserByName_notFound_returnsNull` | `queryForObject` throws `EmptyResultDataAccessException` | returns `null` |
| `getUserByEmail_notFound_returnsNull` | same | returns `null` |
| `getUserById_notFound_returnsNull` | same | returns `null` |
| `hashPassword_delegatesToInjectedEncoder` | call `hashPassword("raw")` | delegates to injected `PasswordEncoder.encode()` |
| `isUserRegistered_returnsTrue` | user exists by name | returns `true` |
| `isUserRegistered_returnsFalse` | user not found | returns `false` |
| `loadUserByUsername_notFound_throwsException` | user not found | throws `UsernameNotFoundException` |

### R10 — Security integration tests (full Spring context) ✅

Naming: `SecurityIT.java` (failsafe picks it up via `*IT` pattern).

Runner: `@RunWith(SpringRunner.class)` + `@SpringBootTest` + `@AutoConfigureMockMvc`.

Required test cases:

| Method name | Scenario | Expected outcome |
|---|---|---|
| `getComments_noAuth_allowed` | GET /comments, no credentials | HTTP 200 |
| `getSession_noAuth_allowed` | GET /session, no credentials | HTTP 200 |
| `postComment_noAuth_returns401` | POST /comment, no credentials | HTTP 401 + JSON body |
| `postComment_withAuth_notUnauthorized` | POST /comment, `@WithMockUser` | status ≠ 401 |
| `sessionCheck_noAuth_returnsUnauthorizedJson` | GET /session, no credentials | HTTP 200, `$.code == 401` |
| `options_noAuth_allowed` | OPTIONS /comment, no credentials | status ≠ 401 |

### R11 — Full business-flow integration test ✅

Naming: `CommentsFlowIT.java`.

The single test method must exercise the complete user journey in order:

1. `POST /user` — register a new unique user → expect `$.code == 200`
2. `POST /user` again (same credentials) — expect `$.code == 500`
3. `POST /session` — form login → expect `$.code == 200`; capture the `HttpSession`
4. `GET /session` with session → expect `$.code == 200` and `$.data.name == username`
5. `GET /comments` without session → expect `$.code == 200` and `$.data` is an array
6. `POST /comment` with session → expect `$.code == 200`
7. `GET /comments` → expect at least one comment in `$.data`
8. `POST /comment` without session → expect HTTP 401

### R12 — Refactor `SortedCommentTest` to pure unit test ✅

- [x] Remove `@SpringBootTest`, `@MockBean`, `@Autowired`.
- [x] Replace with Mockito (`@ExtendWith(MockitoExtension.class)`), `@Mock CommentRepository`, `@InjectMocks CommentService`.
- [x] Remove `try/catch ParseException`; declare `throws ParseException` on the test method instead.

### R13 — Maven test phase separation ✅

- [x] Add `maven-failsafe-plugin` (version `3.0.0-M7`) to `comments-tree-api/pom.xml`.
- [x] Bind goals `integration-test` and `verify`.
- [x] Verified: `mvn test` runs `*Test` only; `mvn verify` runs `*Test` + `*IT` (2026-05-20).

### R14 — H2 test resources ✅

Create `src/test/resources/application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.auto.openurl=false
```

Create `src/test/resources/jdbc/schema.sql` with an H2-compatible version of the production schema (use `AUTO_INCREMENT`, not SQLite's `AUTOINCREMENT`).

### R15 — Taskfile tasks ✅

Added three tasks to `Taskfile.yml` with English `desc` fields:

| Task name | Command | Description |
|---|---|---|
| `test` | `mvn test -pl comments-tree-api -am -DskipFrontend=true` | Run unit tests only |
| `test-it` | `mvn failsafe:integration-test failsafe:verify ...` | Run integration tests only |
| `test-all` | `mvn verify -pl comments-tree-api -am -DskipFrontend=true` | Run unit tests and integration tests |

### R16 — AI documentation ✅

- [x] Create `AGENTS.md` at the repository root covering: project overview, language policy, Java/Spring conventions, testing conventions, key commands.
- [x] Create `.github/copilot-instructions.md` as a concise Copilot-readable version of the same rules.

---

## Constraints

- Java source/target remains `1.8`; do not use Java 9+ language features.
- Spring Boot version stays at `2.6.3`; Spring Security stays at `5.6.x`.
- Do not add new production dependencies beyond what is already declared in `pom.xml`.
- Test dependencies (`spring-security-test`, `junit-vintage-engine`, `h2`) are acceptable additions.
- All new identifiers, comments, log messages, and Javadoc must be in English.
- Chinese text is only acceptable in existing end-user UI strings in the frontend templates.

---

## Acceptance Criteria

All of the following must be true before the task is considered complete:

- [x] `mvn test -pl comments-tree-api -am -DskipFrontend=true` exits 0; only `*Test` classes run; no `*IT` classes run.
- [x] `mvn verify -pl comments-tree-api -am -DskipFrontend=true` exits 0; both `*Test` and `*IT` classes run.
- [ ] Unit tests (`SortedCommentTest`, `UserServiceUnitTest`, `CommentDaoTest`) do **not** load a Spring `ApplicationContext` — `SortedCommentTest` and `UserServiceUnitTest` OK; **`CommentDaoTest` still uses `@SpringBootTest`** (also `UserTest` loads context during `mvn test`).
- [x] Integration tests (`SecurityIT`, `CommentsFlowIT`) load a full Spring context with H2.
- [ ] No `@Autowired` field injection remains in production code — **`BrowserCommandRunner` still has `@Autowired Browser`**.
- [x] No `e.printStackTrace()` remains anywhere in the codebase.
- [x] `SecurityConfig` does not extend `WebSecurityConfigurerAdapter`.
- [x] The application starts without circular dependency errors.
- [x] `task test`, `task test-it`, and `task test-all` are defined in `Taskfile.yml` (not re-run in this audit).
