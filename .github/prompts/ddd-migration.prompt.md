---
mode: agent
description: Incrementally refactor comments-tree-api toward lightweight tactical DDD per the migration task spec.
---

Apply the task spec defined in [docs/specs/ddd-migration.md](../../docs/specs/ddd-migration.md).

Work **one phase at a time** (Phase 1 → Phase 6). Do not start the next phase until every checklist item for the current phase in **Acceptance Criteria** is satisfied.

Default entry point: **Phase 1 (R1–R3)** — extract `CommentTree` only.

After completing a phase, run:

```bash
mvn test -pl comments-tree-api -am -DskipFrontend=true
mvn verify -pl comments-tree-api -am -DskipFrontend=true
```

Report which phase was completed, what changed, and verification results before stopping.
