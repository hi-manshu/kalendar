---
name: No inline comments — KDoc only
description: All code written for this project must not use inline // comments. Only KDoc (/** */) is allowed for documentation.
type: feedback
---

Never write inline `//` comments in Kotlin source files. Use KDoc (`/** */`) on declarations instead.

**Why:** User explicitly prefers KDoc-only style for this codebase. Inline comments clutter the code and should be expressed as KDoc on the relevant function or class instead.

**How to apply:** Any time code is written or edited in this project, strip all `//` comments (including section dividers like `// ── Helpers ──`). If the explanation is worth keeping, convert it to KDoc on the nearest declaration.
