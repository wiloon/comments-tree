---
mode: agent
description: Incrementally refactor comments-tree-api toward lightweight tactical DDD without changing HTTP contracts.
---

# Task Spec: Tactical DDD Migration (comments-tree-api)

## Background

The `comments-tree-api` module uses a classic three-layer layout (Controller → Service → Repository) with an **anemic domain model**:

- `Comment` is used simultaneously as a POST request body (`@Valid`), a SQL join projection, and tree-building input.
- `CommentEntity` is the Spring Data JDBC persistence model.
- `CommentsTreeNode` combines tree structure, sorting rules, and JSON serialisation.
- Core behaviour (closure-table tree assembly, sibling sort by `updateTime`) lives in `CommentService.getSortedComments()`.
- `CommentController` resolves the authenticated user and catches business failures inline instead of delegating to application/domain layers.

The bounded contexts are small but identifiable:

| Context | Scope | Notes |
| --- | --- | --- |
| **Comment** | Threaded comments, tree read model, post/delete | Closure table `comments_tree_path` is infrastructure |
| **Identity** | Users, registration, Spring Security integration | Stays thin; comment context depends on a port only |

This spec defines a **phased, PR-sized migration** toward **lightweight tactical DDD**. It explicitly excludes strategic DDD overhead (microservices, event sourcing, CQRS split, domain events unless a later spec requires them).

**Prerequisite:** `docs/specs/spring-boot-improvements.md` requirements are already satisfied (constructor injection, test pyramid, `GlobalExceptionHandler`, etc.).

---

## Goals

1. **Encapsulate core domain logic** — comment tree assembly and posting rules live in testable domain objects, not in a generic `@Service`.
2. **Separate concerns** — API DTOs, domain models, and persistence entities must not share one class.
3. **Invert persistence dependencies** — the comment bounded context depends on a domain `CommentRepository` interface; JDBC/Spring Data stays in infrastructure.
4. **Thin delivery layer** — controllers translate HTTP to commands/queries; security user resolution goes behind a port.
5. **Zero regression** — after every phase, existing unit/integration/e2e tests pass without changing public HTTP contracts (`GET /comments`, `POST /comment`, JSON shape).

---

## Target Package Layout (end state)

Phases may introduce packages gradually; the **final** layout for the comment context:

```text
com.wiloon.comments.comment
├── domain/
│   ├── Comment.java              # aggregate root (posting behaviour)
│   ├── CommentContent.java       # value object (optional in Phase 5)
│   ├── CommentTree.java          # tree assembly + sort rules
│   ├── CommentRepository.java    # domain port
│   └── CommentReadModel.java     # flat row for tree building (optional rename from Comment projection)
├── application/
│   ├── PostCommentUseCase.java
│   ├── ListCommentTreeUseCase.java
│   ├── PostCommentCommand.java
│   └── CurrentUserResolver.java  # port
├── infrastructure/
│   ├── CommentJdbcRepository.java    # implements domain CommentRepository
│   ├── SpringDataCommentRepository.java  # Spring Data CrudRepository (rename from CommentRepository)
│   ├── CommentEntity.java
│   └── SecurityContextCurrentUserResolver.java
└── interfaces/
    ├── CommentController.java
    ├── CreateCommentRequest.java
    └── CommentsTreeNodeDto.java    # only if JSON mapping must diverge from domain (optional)
```

The **user** context keeps its current package; it exposes no DDD ceremony beyond what Identity already needs.

---

## Migration Phases Overview

| Phase | ID range | Theme | Delivers |
| --- | --- | --- | --- |
| 1 | R1–R3 | Extract `CommentTree` | First rich domain object; pure unit tests |
| 2 | R4–R6 | API DTO for writes | `CreateCommentRequest`; `Comment` no longer POST body |
| 3 | R7–R9 | Repository port | Domain `CommentRepository`; JDBC adapter |
| 4 | R10–R12 | Application layer | Use cases; thin controller |
| 5 | R13–R15 | Aggregate behaviour | Posting rules in domain; optional value objects |
| 6 | R16 | Documentation | Update `AGENTS.md` with layer rules |

**Rule:** Complete and verify one phase before starting the next. Each phase should be a single PR when possible.

---

## Requirements

### Phase 1 — Extract `CommentTree` (do this first)

#### R1 — `CommentTree` domain object

Create `com.wiloon.comments.comment.domain.CommentTree` (package `domain` may be introduced in this phase only for `CommentTree`).

- Move the tree-building loop from `CommentService.getSortedComments()` into `CommentTree`.
- Public API (minimum):

```java
public final class CommentTree {
    public static CommentTree build(List<Comment> flatComments) { ... }
    public TreeSet<CommentsTreeNode> getRootReplies() { ... }
}
```

- Preserve existing behaviour exactly:
  - Virtual parent nodes via `CommentsTreeNode.newNode(int commentId)` when missing.
  - Root replies keyed at `Comment.ROOT_NODE_ID` (`0`).
  - Sibling order: `CommentsTreeNode.compareTo` (newer `updateTime` first).
- `CommentService.getSortedComments()` becomes:

```java
return CommentTree.build(commentRepository.findAllCommentsWithTreePath()).getRootReplies();
```

#### R2 — `CommentTreeTest` unit tests

Create `CommentTreeTest.java` (`*Test`, Mockito not required — pure Java).

| Method name | Scenario | Expected outcome |
| --- | --- | --- |
| `build_emptyList_returnsNullRootReplies` | empty flat list | `getRootReplies()` is `null` |
| `build_onlyRootChildren_returnsSortedTopLevel` | ports existing `SortedCommentTest` fixture | same assertions as today |
| `build_deepNesting_preservesReplyOrder` | multi-level fixture | nested `TreeSet` structure matches golden expectations |

`SortedCommentTest` may be updated to assert via `CommentTree.build(list)` **or** keep mocking `CommentRepository` + `CommentService` — both are acceptable if Phase 1 acceptance criteria pass.

#### R3 — No API or schema changes in Phase 1

- Do not change `CommentController`, URL paths, or JSON field names.
- Do not rename `CommentRepository` (Spring Data) in this phase.

---

### Phase 2 — Separate API model for POST

#### R4 — `CreateCommentRequest`

Create `com.wiloon.comments.comment.interfaces.CreateCommentRequest`:

```java
public record CreateCommentRequest(
    @NotBlank @Size(min = 3, max = 200) String content,
  int parentId
) {}
```

- Move Jakarta validation annotations off `CommentRaw.content` **only if** nothing else relies on them for POST; otherwise duplicate validation on the DTO and leave projection fields unvalidated.
- `POST /comment` accepts `CreateCommentRequest`, not `Comment`.

#### R5 — `CommentController` POST cleanup

- Map `CreateCommentRequest` → service/use-case parameters.
- Remove `try/catch` around save in favour of `GlobalExceptionHandler` (let domain/application throw checked runtime exceptions with clear messages, or use existing `CommonResult` only for expected business failures).
- Authenticated user resolution may remain in the controller **until Phase 4**.

#### R6 — Phase 2 tests

- `CommentsFlowIT` and `SecurityIT` must pass unchanged (same endpoints and status codes).
- Add `CreateCommentRequestTest` or extend an existing unit test only if validation behaviour needs explicit coverage.

---

### Phase 3 — Domain repository port

#### R7 — Domain `CommentRepository` interface

In `comment.domain`:

```java
public interface CommentRepository {
    List<Comment> findAllForTree();
    int saveNewReply(String content, String userId, int parentId);
    void deleteById(int id);
}
```

- `findAllForTree()` replaces direct use of `findAllCommentsWithTreePath()` in application/domain code.
- `saveNewReply` encapsulates insert into `comments` + `comments_tree_path` (today's `newComment` behaviour).

#### R8 — Infrastructure adapter

- Rename the Spring Data interface:
  - `CommentRepository` → `SpringDataCommentRepository` (or `CommentJdbcRepository` if it holds `@Query` methods).
- Implement `domain.CommentRepository` in `CommentJdbcRepository` (class name negotiable, must implement the domain interface).
- Mark the adapter `@Repository`; wire it as the domain port bean (Spring will inject the domain interface where needed).

#### R9 — `CommentService` delegates to port

- `CommentService` depends on `domain.CommentRepository`, not Spring Data directly.
- Alternatively, begin deprecating `CommentService` in favour of use cases (Phase 4); do **not** delete `CommentService` until Phase 4 substitutes it.

Phase 3 acceptance: all existing tests green; `CommentDaoTest` / `SortedCommentTest` updated to mock `domain.CommentRepository` if package types change.

---

### Phase 4 — Application layer (use cases)

#### R10 — `ListCommentTreeUseCase`

```java
@Service
public class ListCommentTreeUseCase {
    public TreeSet<CommentsTreeNode> execute() {
        return CommentTree.build(commentRepository.findAllForTree()).getRootReplies();
    }
}
```

#### R11 — `PostCommentUseCase`

```java
public record PostCommentCommand(String content, int parentId) {}

@Service
public class PostCommentUseCase {
    public int execute(PostCommentCommand command, String userId) { ... }
}
```

- `@Transactional` on `execute` (application layer) or on repository adapter — pick one and document in `AGENTS.md`.
- Validates authenticated user id is non-null; throws a domain- or application-level exception if missing (handled by `GlobalExceptionHandler`).

#### R12 — Thin `CommentController`

| Endpoint | Delegates to |
| --- | --- |
| `GET /comments` | `ListCommentTreeUseCase` |
| `POST /comment` | `CurrentUserResolver` + `PostCommentUseCase` |

Introduce port:

```java
public interface CurrentUserResolver {
    String requireUserId(); // throws if unauthenticated or user not found
}
```

Implementation: `SecurityContextCurrentUserResolver` in `infrastructure`, using `UserService` internally.

- Remove direct `UserService` usage from `CommentController`.
- `CommentService` may be deleted **only after** all callers use use cases and tests are updated.

---

### Phase 5 — Richer aggregate (optional but specified)

#### R13 — `Comment.post` factory or instance method

Move rules from `saveNewReply` into `domain.Comment`:

- Reject blank / too-short content (mirror validation limits).
- Reject invalid `parentId` when parent does not exist (except `ROOT_NODE_ID`).
- Repository called only from aggregate or a domain service — not from controller.

#### R14 — Optional value objects

Only if they simplify validation without ceremony:

- `CommentContent` wrapping `String` with length rules.
- Do **not** introduce value objects for `Timestamp`, `Integer` id, or UUID user id.

#### R15 — Rename projection type (optional)

If `Comment` still confuses aggregate vs read model, introduce `CommentReadModel` for SQL join results and reserve `Comment` for the aggregate. Update mappers and `CommentTree.build` signature accordingly. This is optional; skip if rename churn outweighs clarity.

---

### Phase 6 — Agent documentation

#### R16 — Update `AGENTS.md`

Add a **DDD layers (comment context)** section covering:

- Package layout and dependency direction (`interfaces` → `application` → `domain` ← `infrastructure`).
- Where `@Transactional` lives.
- Rule: no Spring annotations on domain classes.
- Rule: new comment features extend domain/application first, not controller.

---

## Constraints

- **Java 17**; follow existing Spring Boot **3.5.x** stack.
- **No new production dependencies** (no Axon, MapStruct required, jMolecules, etc.). Manual mapping is fine.
- **Constructor injection only**; `private final` dependencies.
- **English** for all new identifiers, comments, logs, Javadoc, and spec text.
- **HTTP contract frozen** unless a future spec explicitly versions the API:
  - `GET /comments` → `CommonResult` with `TreeSet`-compatible JSON (field names unchanged).
  - `POST /comment` → body fields `content`, `parentId`; response messages may stay as today.
- **Persistence** remains Spring Data JDBC + SQLite (prod) / H2 (test); closure table stays in infrastructure.
- **Do not** split into separate deployable services or modules in this spec.
- **User context** does not require use cases unless a change touches registration/login; keep `UserService` as `UserDetailsService`.

---

## Out of Scope

- Event sourcing, domain events, CQRS read/write stores.
- API versioning (`/v2/...`).
- Frontend TypeScript changes (unless response shape breaks — must not happen).
- Extracting `CommentsTreeNode` to a separate DTO unless serialisation requirements change.
- Full ubiquitous language workshop / event storming documentation.

---

## Verification Commands

After **each** phase:

```bash
mvn test -pl comments-tree-api -am -DskipFrontend=true
mvn verify -pl comments-tree-api -am -DskipFrontend=true
```

From repo root (if Taskfile tasks exist):

```bash
task test
task test-all
```

---

## Acceptance Criteria

### Phase 1 complete

- [ ] `CommentTree` exists; `CommentService` delegates tree building to it.
- [ ] `CommentTreeTest` passes; `SortedCommentTest` passes.
- [ ] `mvn test` and `mvn verify` exit 0.
- [ ] No HTTP or OpenAPI behaviour change.

### Phase 2 complete

- [ ] `POST /comment` uses `CreateCommentRequest`.
- [ ] `CommentsFlowIT`, `SecurityIT` pass.
- [ ] Validation errors still return 400 for malformed bodies (if applicable).

### Phase 3 complete

- [ ] Domain `CommentRepository` interface exists; Spring Data type renamed.
- [ ] No production class in `domain` imports `org.springframework.data.*`.
- [ ] All tests green.

### Phase 4 complete

- [ ] `ListCommentTreeUseCase` and `PostCommentUseCase` exist.
- [ ] `CommentController` has no `UserService` dependency.
- [ ] `CurrentUserResolver` implemented in infrastructure.
- [ ] `CommentService` removed or reduced to a deprecated stub with zero callers.

### Phase 5 complete (optional)

- [ ] Posting validation and parent checks live in `domain.Comment` (or dedicated domain service).
- [ ] Unit tests cover rejection paths (invalid parent, blank content).

### Phase 6 complete

- [ ] `AGENTS.md` documents layer rules and dependency direction.

### Overall migration complete

- [ ] All phase checklists above are checked.
- [ ] `comment` package matches target layout (or documented intentional deviations in `AGENTS.md`).
- [ ] No `@Autowired` field injection; no Chinese in new Java comments/logs.
- [ ] Full verify + Playwright e2e (if run in CI) pass.

---

## Suggested PR Sequence

| PR | Phase | Title (example) |
| --- | --- | --- |
| 1 | 1 | `refactor(comment): extract CommentTree domain object` |
| 2 | 2 | `refactor(comment): add CreateCommentRequest DTO` |
| 3 | 3 | `refactor(comment): introduce domain CommentRepository port` |
| 4 | 4 | `refactor(comment): add post/list use cases and CurrentUserResolver` |
| 5 | 5 | `feat(comment): move posting rules into Comment aggregate` |
| 6 | 6 | `docs: document DDD layers in AGENTS.md` |

---

## References

- Current tree logic: `CommentService.getSortedComments()`
- Golden unit fixture: `SortedCommentTest.testSortedComments`
- Full HTTP journey: `CommentsFlowIT`
- Project conventions: `/AGENTS.md`
