# QA Fix Implementation Plan

**Status:** Draft  
**Source:** Senior QA defect table (QA-001 … QA-036)  
**Goal:** Close all Critical / Major / Minor / Process defects with shippable PR slices, tests, and docs.  
**Branch strategy:** Prefer stacked PRs on `feat/qa-hardening` (or Graphite stack) → merge to `main` / release branch.

---

## 1. Principles

| Principle | Rule |
|-----------|------|
| Contract first | Fix public API behaviour before cosmetics |
| Test with the bug | Every Critical/Major fix needs a unit or UI regression test |
| No silent no-ops | Unsupported modes fail loud or are documented + type-restricted |
| Pad-aware | Month grids always treat previous/next month pad cells consistently |
| Docs = product | Doc/example changes ship in same PR as behaviour when possible |
| Small PRs | One theme per PR; stack if needed |

**Out of scope for “fix all” product code:** full SLSA provenance, multi-repo Dependabot, performance rewrite of infinite pager (profile first under QA-027).

---

## 2. Target architecture (selection + disable)

Single source of truth for “can this date be interacted with?”:

```text
isInteractable(date) =
  !config.disabledDates(date)
  && !isDateOutOfBounds(date, min, max)
  && (optional) isInPrimaryPeriod(date)   // current month / week / season month
```

Apply in:

1. Default `KalendarDay` / mini cells  
2. **`dayContent` path** (pass flag or gate clicks)  
3. `LocalDate.onDayClick` (reject non-interactable)  
4. Range builder (exclude or refuse ranges that contain non-interactable days — decide in PR-A)  
5. Agenda row clicks  

### Recommended API change (PR-A)

```kotlin
// Before
dayContent: (@Composable (date, isSelected, events) -> Unit)?

// After (source-compatible overload or defaulted 4th param)
dayContent: (@Composable (
    date: LocalDate,
    isSelected: Boolean,
    events: List<KalendarEvent>,
    isDisabled: Boolean,
) -> Unit)?
```

Deprecation plan:

- Keep 3-arg overload as wrapper that calls 4-arg with `isDisabled = false` **only if** we cannot break RC consumers — **prefer breaking 4-arg** while still `2.0.0-RC*` (cheaper than dual maintenance).

---

## 3. Work packages (PR plan)

### Legend

| Field | Meaning |
|-------|---------|
| **IDs** | QA defect IDs closed |
| **Risk** | Merge risk |
| **Est.** | Engineer-days (1 person) |

---

### PR-1 — Selection & disable contract (Critical)

| | |
|--|--|
| **IDs** | QA-001, QA-002, QA-006, QA-022 (partial), QA-023 (partial) |
| **Risk** | High (API) |
| **Est.** | 2–3 d |
| **Modules** | `kalendar`, `kalendar-foundation` |

**Implement**

1. Introduce `isDateInteractable(date, config, isPrimaryPeriod: Boolean)` in foundation or kalendar util.  
2. Extend `dayContent` to include `isDisabled: Boolean` (all types that support `dayContent`: Oceanic, Firey, Aerial, Solaris, Yearly, Season).  
3. When `dayContent == null`, behaviour unchanged.  
4. When `dayContent != null`, still compute `isDisabled` and pass it; **document** that host must honour it (library cannot force).  
5. Gate `LocalDate.onDayClick`: if not interactable, return early (no state mutation, no callback).  
6. Range: if second tap lands on disabled, ignore; if intermediate days include disabled, either (A) allow visual range but filter events, or (B) refuse complete — **choose B** for booking safety.  
7. Optional: `KalendarDay.selectedDate: LocalDate? = null` instead of default `= date` (QA-022) — fix default selected logic.

**Tests**

- Unit: `onDayClick` ignores disabled.  
- Unit: range refuses end on disabled / spanning disabled (per chosen rule).  
- Unit: `isDateInteractable` matrix.

**Docs**

- `doc/Config.md` + each type doc: dayContent signature + disable rules.

**Acceptance**

- [ ] Custom dayContent receives correct `isDisabled` for past days when `disabledDates = { it < today }`.  
- [ ] Tapping disabled never fires `OnDaySelectionAction`.  
- [ ] Existing unit suite green.

---

### PR-2 — Header forward navigation (Critical)

| | |
|--|--|
| **IDs** | QA-003, QA-017, QA-030 |
| **Risk** | Medium |
| **Est.** | 0.5–1 d |
| **Modules** | `kalendar-foundation`, `kalendar` |

**Implement**

1. Add `canNavigateForward: Boolean = true` to both `KalendarHeader` overloads + content.  
2. Next `IconButton` `enabled = canNavigateForward`.  
3. Wire Oceanic, Firey, Season, Yearly (and any other arrow headers).  
4. Fix contentDescription: “Previous/Next month|week|season|year” via parameter `navigationLabel` or type-specific strings (QA-030).

**Tests**

- If no Compose UI test yet: unit pure logic for canGoBack/Forward already exists partially — add header preview test later in PR-8.  
- Manual checklist until PR-8.

**Acceptance**

- [ ] With `maxDate` in current month, next arrow visually disabled and no-op.  
- [ ] With `minDate`, prev disabled.

---

### PR-3 — Agenda parity (Critical + Major)

| | |
|--|--|
| **IDs** | QA-004, QA-005, QA-019, QA-020 |
| **Risk** | Medium |
| **Est.** | 1–2 d |
| **Modules** | `kalendar` |

**Implement**

1. **Range:** Either implement proper range selection across agenda rows **or** seal Agenda to only Single (and document Multiple). Preferred:  
   - Single: keep  
   - Multiple: toggle set of dates that have events  
   - Range: first/second date header or row tap builds range; call `onRangeSelected`  
2. Apply `disabledDates` + bounds before firing callbacks.  
3. Keys: prefer `event.id` if added, else `"${date}_${index}_${eventName}"` without hashCode-only (QA-019). Optional: add optional `id: String?` on `KalendarEvent` (default null) — if too big, index key is enough.  
4. Locale-aware date header (QA-020): use `dayNameFormatter`-style or kotlinx formatting helper.

**Tests**

- Agenda grouping sort order (existing event tests).  
- New: range callback with two taps; disabled date not selectable.

**Acceptance**

- [ ] No silent `Unit` for Range.  
- [ ] Past-disabled dates not clickable when configured.

---

### PR-4 — Sample app correctness (Major)

| | |
|--|--|
| **IDs** | QA-007, QA-016, QA-035 |
| **Risk** | Low |
| **Est.** | 1 d |
| **Modules** | `sample` |

**Implement**

1. After PR-1: use 4-arg `dayContent`; set `isDisabled` from library.  
2. Also treat non-primary-month pad as disabled (if library flag not already true).  
3. Distinct styles: series highlight vs user selection (border vs fill, or different colours) — QA-016.  
4. Lightweight type switcher (Oceanic / Firey / Season / Agenda) for QA matrix — QA-035.  
5. Fixture events + `disabledDates = { it < today }`.

**Acceptance**

- [ ] Cannot select yesterday or other-month pad.  
- [ ] Series days visually distinct from tapped day.  
- [ ] Switcher covers ≥4 types.

---

### PR-5 — Yearly / Season period correctness (Major)

| | |
|--|--|
| **IDs** | QA-009, QA-024 |
| **Risk** | Low–Med |
| **Est.** | 1 d |
| **Modules** | `kalendar` |

**Implement**

1. Yearly mini grid: `date.month == month && date.year == year` (QA-009).  
2. Season: document northern meteorological only; add `KalendarHemisphere` or `seasonMode` config **or** explicit KDoc + README limitation (QA-024).  
   - Minimal fix: docs only.  
   - Full fix: `enum class KalendarHemisphere { Northern, Southern }` mapping months.

**Tests**

- Yearly: January pad December previous year not rendered as in-month.  
- Season: winter cross-year already tested; add SH mapping if implemented.

**Acceptance**

- [ ] No wrong-year cell treated as in-month in Yearly.  
- [ ] Hemisphere documented or implemented.

---

### PR-6 — Visible range contract + Firey bounds (Major/Minor)

| | |
|--|--|
| **IDs** | QA-008, QA-018 |
| **Risk** | Medium (behaviour) |
| **Est.** | 1 d |
| **Modules** | `kalendar`, docs |

**Implement**

1. Document that `onVisibleRangeChange` is **grid-inclusive** (pad in/out).  
2. Optional config:

```kotlin
enum class VisibleRangeMode { GridInclusive, PrimaryPeriodOnly }
// default: GridInclusive (current correct behaviour for event load)
```

3. Firey: align `canGoBack`/`canGoForward` with day-level min/max (week may partially overlap) — define: disable nav only when entire next/prev week is out of bounds **or** when no interactable day remains.

**Tests**

- Visible range for Jan 2026 Monday-first starts `2025-12-29`.  
- Firey nav matrix around minDate mid-week.

**Acceptance**

- [ ] Docs match implementation.  
- [ ] Hosts can opt into month-only if flag added.

---

### PR-7 — Documentation & changelog (Major)

| | |
|--|--|
| **IDs** | QA-010, QA-011 |
| **Risk** | Low |
| **Est.** | 0.5 d |
| **Modules** | `doc/`, `README`, `CHANGELOG` |

**Implement**

1. Oceanic/Solaris examples: `MONDAY` default or explicit “US Sunday-start example”.  
2. README: `kalendar-sync` install + platform matrix.  
3. CHANGELOG: Yearly, Agenda, Season, pad-aware visible range, dayContent disable, header forward.  
4. Cross-link `Learn-Kalendar.md` if needed.

**Acceptance**

- [ ] No doc claims contradict `KalendarConfig` defaults.  
- [ ] Sync discoverable from README.

---

### PR-8 — UI test foundation (Major)

| | |
|--|--|
| **IDs** | QA-012, QA-029 (partial) |
| **Risk** | Med (infra) |
| **Est.** | 2–3 d |
| **Modules** | `kalendar` or `sample`, CI |

**Implement**

1. Add Compose Multiplatform UI test dependency on desktop JVM (fastest feedback).  
2. Scenarios:  
   - Disabled past date not clickable (default dayContent null).  
   - Next arrow disabled at maxDate.  
   - Oceanic Monday-first first cell previous month.  
3. Optional Android instrumented smoke in nightly only.  
4. Semantics: selected/disabled on `KalendarDay` (QA-029 starter).

**CI**

- Run desktop Compose UI tests in main CI job.

**Acceptance**

- [ ] ≥3 UI tests green in CI.  
- [ ] QA-003 regression locked by test.

---

### PR-9 — Full CI on fork + iOS compile (Major)

| | |
|--|--|
| **IDs** | QA-013, QA-014 |
| **Risk** | Low |
| **Est.** | 1 d |
| **Modules** | `.github/workflows` |

**Implement**

1. Ensure `ci.yml` present on fork: detekt, desktopTest, compile desktop/wasm/android, macOS iOS simulator compile.  
2. Branch triggers: `main` + `feat/**` PRs.  
3. Path filters for APK job (skip pure docs).  
4. Optional: iosSimulatorArm64Test when tests exist.  
5. Composite action for JDK+Android+Gradle setup (DRY).

**Acceptance**

- [ ] PR to feat branch runs full quality job.  
- [ ] iOS compile job on macos-latest.

---

### PR-10 — API binary compatibility (Major)

| | |
|--|--|
| **IDs** | QA-015 |
| **Risk** | Med |
| **Est.** | 1 d |
| **Modules** | `kalendar`, `kalendar-foundation`, `kalendar-sync` |

**Implement**

1. Add `binary-compatibility-validator`.  
2. Generate/check `.api` files.  
3. CI fails on ABI break.  
4. Document dump update process in CONTRIBUTING.

**Acceptance**

- [ ] CI check green; intentional breaks require `.api` update in same PR.

---

### PR-11 — Sync hardening (Minor)

| | |
|--|--|
| **IDs** | QA-025, QA-026 |
| **Risk** | Med |
| **Est.** | 1–2 d |
| **Modules** | `kalendar-sync`, `sample`, docs |

**Implement**

1. README/sample: handle `NotSupported` / permission denied.  
2. Android: Robolectric or instrumented smoke for insert/fetch if feasible; else documented manual test script.  
3. iOS: manual test checklist in `doc/`.  
4. ICS round-trip already unit-tested — keep as primary automated path.

**Acceptance**

- [ ] Sample or doc shows non-happy path.  
- [ ] At least one automated Android-oriented test **or** signed manual QA script.

---

### PR-12 — Code structure (Minor)

| | |
|--|--|
| **IDs** | QA-028, QA-023 |
| **Risk** | High (refactor) |
| **Est.** | 2–3 d |
| **Modules** | `kalendar` |

**Implement**

1. Extract `KalendarSelectionState` (single/multiple/range) used by Oceanic/Firey/Aerial/Solaris.  
2. Extract shared “month body” / “week body” composables.  
3. Document controlled selection: `selectedDate` + `onDaySelectionAction` contract; optional future `SelectionMode.Controlled`.  
4. Do **after** PR-1 so behaviour tests exist.

**Acceptance**

- [ ] No behaviour change (UI tests + unit).  
- [ ] LOC duplication reduced measurably.

---

### PR-13 — A11y polish (Cosmetic)

| | |
|--|--|
| **IDs** | QA-029, QA-030 (remainder) |
| **Risk** | Low |
| **Est.** | 1 d |
| **Modules** | foundation day/header |

**Implement**

1. Semantics: role, selected, disabled, “dd MMMM yyyy”.  
2. Header CD from PR-2 completed.  
3. Manual TalkBack/VoiceOver checklist in `doc/A11y.md`.

**Acceptance**

- [ ] Checklist completed on Android sample.

---

### PR-14 — DevOps / supply chain (Process)

| | |
|--|--|
| **IDs** | QA-031, QA-032, QA-033, QA-034 |
| **Risk** | Low–Med |
| **Est.** | 1–2 d |
| **Modules** | `.github`, root |

**Implement**

1. Stop ignoring `gradle-wrapper.jar` if not tracked; add `gradle/actions/wrapper-validation`.  
2. Dependabot `github-actions` ecosystem.  
3. `SECURITY.md`, CodeQL workflow, dependency-review on PR, upload Detekt SARIF to code scanning.  
4. Release: GitHub Environment `release` with reviewers; prefer tag → publish order or dry-run input; `doc/Releasing.md`.  
5. Node 20 deprecation: bump actions when compatible.

**Acceptance**

- [ ] Wrapper validation on every CI.  
- [ ] Release requires approval.  
- [ ] SECURITY.md present.

---

### PR-15 — Performance follow-up (Info → optional)

| | |
|--|--|
| **IDs** | QA-027 |
| **Risk** | Med |
| **Est.** | 1–2 d after profiling |
| **Modules** | Aerial, Solaris |

**Implement only if profiling shows issue**

1. Bound pager window or use finite page count with min/max.  
2. Avoid heavy work per page composition.

**Acceptance**

- [ ] Profile notes attached; no regression in UI tests.

---

## 4. Dependency graph

```text
PR-1 (selection contract)
  ├── PR-2 (header)          [parallel OK]
  ├── PR-3 (agenda)          [after PR-1 preferred]
  ├── PR-4 (sample)          [after PR-1]
  ├── PR-5 (yearly/season)   [parallel OK]
  ├── PR-6 (visible range)   [parallel OK]
  ├── PR-7 (docs)            [after 1–6 content stable, or incremental]
  ├── PR-8 (UI tests)        [after PR-1 + PR-2]
  ├── PR-12 (refactor)       [after PR-1 + PR-8]
  └── PR-13 (a11y)           [after PR-2]

PR-9 (CI)     ── parallel anytime
PR-10 (BCV)   ── after API-shaping PRs 1–3 land (or baseline before, update after)
PR-11 (sync)  ── parallel
PR-14 (devops)── parallel
PR-15         ── optional last
```

**Critical path to “safe RC”:**  
`PR-1 → PR-2 → PR-3 → PR-4 → PR-8 → PR-7 → PR-9`

**Full “fix all”:** critical path + PR-5,6,10,11,12,13,14 (+15).

---

## 5. Suggested sprint schedule

| Sprint | PRs | Outcome |
|--------|-----|---------|
| **S1 (week 1)** | 1, 2, 9 | Selection contract + header + full CI |
| **S2 (week 2)** | 3, 4, 5, 6, 7 | Agenda, sample, period math, visible range, docs |
| **S3 (week 3)** | 8, 10, 14 | UI tests, ABI, DevOps |
| **S4 (week 4)** | 11, 12, 13, (15) | Sync, refactor, a11y, perf if needed |

---

## 6. Test strategy matrix

| Layer | What | PRs |
|-------|------|-----|
| Unit (desktop) | Date grid, interactable, onDayClick, range, season, yearly year check | 1, 5, 6 |
| Unit (sync) | ICS, RRULE (existing) + result handling | 11 |
| Compose UI (desktop) | Disable, maxDate arrow, pad Monday | 8 |
| Instrumented (optional nightly) | Sample launch, one tap | 8, 9 |
| Manual | TalkBack, real calendar permission Android/iOS | 11, 13 |
| CI gates | detekt, unit, UI, BCV, wrapper validation | 9, 10, 14 |

---

## 7. API / versioning policy

| Change | Version impact |
|--------|----------------|
| `dayContent` 4th param | **Breaking** if no overload — OK in `2.0.0-RC*`; if stable 2.0.0 already published, add overload |
| `canNavigateForward` on header | Additive (non-breaking) |
| `VisibleRangeMode` | Additive |
| `KalendarEvent.id` optional | Additive |
| Default `selectedDate` nullability on `KalendarDay` | Potentially breaking — isolate |

Ship notes in CHANGELOG under **Breaking** / **Fixed** / **Added**.

---

## 8. Definition of Done (global)

All of the following:

- [ ] Every QA-001…QA-036 mapped to **Fixed** or **Won’t fix (documented)** in this plan’s tracking table  
- [ ] No Critical open  
- [ ] No Major open without explicit waiver  
- [ ] Desktop unit + UI tests green in CI  
- [ ] Detekt green  
- [ ] Sample demonstrates disable-before-today + Monday-first + pad-safe series  
- [ ] README + Config docs accurate  
- [ ] Release runbook updated if publish flow changed  

---

## 9. Tracking table (close-out)

| ID | Severity | PR | Status |
|----|----------|-----|--------|
| QA-001 | Critical | PR-1 | Planned |
| QA-002 | Critical | PR-1 | Planned |
| QA-003 | Critical | PR-2 | Planned |
| QA-004 | Critical | PR-3 | Planned |
| QA-005 | Major | PR-3 | Planned |
| QA-006 | Major | PR-1 | Planned |
| QA-007 | Major | PR-4 | Fixed (in tree) |
| QA-008 | Major | PR-6 | Planned |
| QA-009 | Major | PR-5 | Fixed (in tree, earlier) |
| QA-010 | Major | PR-7 | Planned |
| QA-011 | Major | PR-7 | Planned |
| QA-012 | Major | PR-8 | Fixed (in tree) |
| QA-013 | Major | PR-9 | Fixed (in tree) |
| QA-014 | Major | PR-9 | Fixed (in tree, iOS job) |
| QA-015 | Major | PR-10 | Planned |
| QA-016 | Major | PR-4 | Fixed (in tree) |
| QA-017 | Minor | PR-2 | Planned |
| QA-018 | Minor | PR-6 | Planned |
| QA-019 | Minor | PR-3 | Planned |
| QA-020 | Minor | PR-3 | Planned |
| QA-021 | Minor | PR-7 (+ optional code later) | Planned |
| QA-022 | Minor | PR-1 | Planned |
| QA-023 | Minor | PR-12 | Planned |
| QA-024 | Minor | PR-5 | Planned |
| QA-025 | Minor | PR-11 | Planned |
| QA-026 | Minor | PR-11 | Planned |
| QA-027 | Info | PR-15 | Optional |
| QA-028 | Minor | PR-12 | Planned |
| QA-029 | Cosmetic | PR-8 / PR-13 | Partial (semantics in tree) |
| QA-030 | Cosmetic | PR-2 / PR-13 | Planned |
| QA-031 | Process | PR-14 | Partial (wrapper-validation in CI) |
| QA-032 | Process | PR-14 | Planned |
| QA-033 | Process | PR-14 | Planned |
| QA-034 | Process | PR-14 | Planned |
| QA-035 | Process | PR-4 | Fixed (in tree) |
| QA-036 | Info | n/a (meta) | N/A |

---

## 10. Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Breaking `dayContent` for RC users | Overload + deprecation one RC cycle, or single break while still RC |
| Range-disabled policy debates | Decide in PR-1 design note; document in Config.md |
| UI tests flaky on CI | Prefer desktop JVM Compose tests; retry once; no emulator in required gate |
| Refactor PR-12 regressions | Only after UI tests (PR-8) |
| Fork vs upstream divergence | Land CI files on both remotes; cherry-pick stack |

---

## 11. Execution checklist (orchestrator)

1. Create branch `feat/qa-hardening` from current mainline feature branch.  
2. Land **PR-1** first; do not start PR-12 until PR-8 green.  
3. Run after each PR:  
   `./gradlew detekt desktopTest` (+ UI tests when present)  
4. Update this file’s tracking **Status** column per merge.  
5. When Critical+Major = Fixed → cut RC build + sample APK workflow.  
6. When all Planned rows Fixed/Waived → tag release candidate / final.

---

## 12. Immediate next actions (start tomorrow)

1. Spike 2h: finalize `dayContent` 4-arg vs overload (API review).  
2. Open PR-1 with `isDateInteractable` + `onDayClick` gate + tests (no sample yet).  
3. Parallel: PR-2 header `canNavigateForward` (tiny).  
4. Parallel: PR-9 copy/enable full `ci.yml` on fork.

---

*End of plan. Source defects: senior QA analysis QA-001–QA-036.*
