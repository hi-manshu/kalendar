# Learn Kalendar — A First-Year Engineer's Guide

> **Who this is for:** You just started engineering (or software). You may know a little programming.
> You do **not** need to know Android, Kotlin, or Compose yet.
>
> **What you will learn:** What this project is, how a calendar UI library is built, how the folders
> fit together, and how a real open-source multiplatform library is organized.

Take it slow. Each section builds on the previous one.

---

## 1. What problem does this project solve?

Almost every app eventually needs a **calendar**:

- Booking a doctor appointment
- Marking holidays
- Showing when you have meetings
- Picking a travel date range

If every team rebuilt a calendar from scratch, they would rewrite the same grids, colors, and
tap-handling again and again.

**Kalendar** is a **library**: a ready-made piece of software other apps can import and use.

Think of it like this:

| Analogy | Meaning in this project |
|--------|-------------------------|
| A **library** | A box of Lego pieces other builders can reuse |
| An **app** | A finished house built with those pieces |
| **Kalendar** | The Lego set for "calendar screens" |

Your app says: “Show me a month view. Highlight today. When the user taps a day, tell me.”
Kalendar draws the UI and reports the taps.

---

## 2. Big idea in one picture

```
┌─────────────────────────────────────────────────────────┐
|  Your app (or the sample app)                           |
|                                                         |
|    Kalendar(                                            |
|      type = Oceanic,     ← which layout?                |
|      events = [...],     ← meetings / holidays          |
|      onDayClick = ...    ← what happens on tap          |
|    )                                                    |
└───────────────────────────┬─────────────────────────────┘
                            │ uses
                            ▼
┌─────────────────────────────────────────────────────────┐
|  kalendar module                                        |
|  (7 different calendar “styles”)                        |
└───────────────────────────┬─────────────────────────────┘
                            │ built from
                            ▼
┌─────────────────────────────────────────────────────────┐
|  kalendar-foundation module                             |
|  (day cells, header, colors, selection logic)           |
└─────────────────────────────────────────────────────────┘

Optional extra:
┌─────────────────────────────────────────────────────────┐
|  kalendar-sync                                          |
|  Read/write the phone’s real calendar + ICS files       |
└─────────────────────────────────────────────────────────┘
```

You do **not** need all three modules at once:

1. Most apps only need **`kalendar`** + **`kalendar-foundation`**.
2. Only if you want device calendar import/export do you add **`kalendar-sync`**.

---

## 3. Words you will see a lot

### 3.1 User interface (UI)

Everything the user **sees and taps**: buttons, text, day circles, arrows.

### 3.2 Component

A **reusable UI piece**, like a brick.

Examples in Kalendar:

- One **day cell** (the circle with `15` inside)
- The **header** (`March 2026` + left/right arrows)
- The whole **month grid**

### 3.3 Library vs application

| | Library | Application (app) |
|--|---------|-------------------|
| Goal | Be useful **inside other projects** | Be used by **people** |
| Example here | `kalendar`, `kalendar-foundation` | `sample` (demo) |
| Users | Other programmers | End users |

### 3.4 Kotlin

The **programming language** this project is written in. Related to Java, popular for Android and
now for multiplatform code.

### 3.5 Jetpack Compose / Compose Multiplatform

A modern way to build UI in Kotlin:

- You **describe** what the screen should look like (“show a row of 7 day names”).
- The system **draws** it and updates it when data changes.

Kalendar’s public API is mostly **Composable functions** — functions that draw UI.

### 3.6 Multiplatform (KMP)

**One codebase**, several places it can run:

| Target | Where it runs |
|--------|----------------|
| Android | Phones / tablets |
| iOS | iPhone / iPad |
| Desktop | Windows / macOS / Linux JVM apps |
| wasmJs | Web browser (WebAssembly) |

Old approach: rewrite the calendar four times.  
Kalendar approach: share most of the logic and UI once.

### 3.7 Module

A **folder that is its own mini-project** with its own build file. The whole repo is several
modules glued together by Gradle (the build tool).

---

## 4. Tour of the repository (folders)

When you open the project root, focus on these:

```
Kalendar/
├── kalendar/                 ← main calendar UI library
├── kalendar-foundation/      ← shared building blocks
├── kalendar-sync/            ← phone calendar + ICS helpers
├── sample/                   ← demo app that uses the library
├── iosApp/                   ← thin iOS shell for the sample
├── doc/                      ← human documentation (you are here)
├── img/                      ← screenshots for the README
├── .github/workflows/        ← automated CI (tests on GitHub)
├── gradle/                   ← dependency versions
├── build.gradle.kts          ← root build setup
├── settings.gradle.kts       ← list of modules
└── README.md                 ← project homepage for GitHub
```

### What each module is for (student version)

#### `kalendar-foundation` — the toolbox

Contains pieces every calendar style needs:

- How a **single day** looks
- How the **title row** looks
- What an **event** is
- How **selection** works (one day, many days, or a range)
- Shared **colors** and **settings**

If Oceanic and Firey both need a day circle, that circle lives in foundation so nobody copies it twice.

#### `kalendar` — the finished calendar styles

Public entry point: `Kalendar(...)`.

You choose a **type** (style). The library switches to the matching screen.

#### `kalendar-sync` — talk to the real world

Optional. Reads and writes events on the **device calendar** (Android / iOS), and can import/export
**ICS** files (the standard “calendar invite” file format).

Desktop and web mostly support ICS, not the system calendar.

#### `sample` — playground

A tiny multiplatform app. Developers change `App.kt`, run it, and see the calendar live.
It is **not** the library users install; it is a **demo**.

---

## 5. The seven calendar “types”

All types share one call shape. Only the **layout and navigation** change.

| Type | What you see | How you move |
|------|----------------|--------------|
| **Oceanic** | Full **month** grid | Previous / next **arrows** |
| **Firey** | One **week** row | Arrows |
| **Solaris** | Full **month** grid | **Swipe** left/right |
| **Aerial** | One **week** row | **Swipe** |
| **Yearly** | All **12 months** of a year | Arrows between years |
| **Season** | **3 months** of a meteorological season | Arrows between seasons |
| **Agenda** | **List of events** by date | Scroll |

### Why so many types?

Different products need different focus:

- A hotel booking screen → month grid (Oceanic / Solaris)
- A “this week” planner → week row (Firey / Aerial)
- An HR holiday overview → year view
- A weather or school-term product → season view
- A meeting list → agenda

One library, many layouts — like one camera with several lenses.

### Season (quick intuition)

Meteorological seasons (northern hemisphere):

| Season | Months |
|--------|--------|
| Spring | March, April, May |
| Summer | June, July, August |
| Autumn | September, October, November |
| Winter | December, January, February |

Winter is special: December is year **Y**, January/February are year **Y+1**.

---

## 6. How an app uses Kalendar (the public API)

Almost everything goes through one function:

```kotlin
Kalendar(
    type = KalendarType.Oceanic,
    selectedDate = today,
    events = myEvents,
    onDaySelectionAction = OnDaySelectionAction.Single { date, eventsOnThatDay ->
        // Your code: open details, save choice, etc.
    },
    config = KalendarConfig(
        startDayOfWeek = DayOfWeek.MONDAY,
    ),
)
```

### Parameters explained like a beginner

| Parameter | Plain English |
|-----------|----------------|
| `type` | Which calendar layout to show |
| `selectedDate` | Which day starts highlighted (often “today”) |
| `events` | List of things that happen on days (meetings, etc.) |
| `onDaySelectionAction` | What happens when the user taps days |
| `config` | Colors, arrows, min/max dates, week start day, … |
| `controller` | Optional remote control: jump to a date from code |
| `dayContent` | Optional: draw your own day cell instead of the default |

You do **not** pass layout math yourself. You pass **data + policy**; the library handles drawing.

---

## 7. Events — “something happens on this day”

A calendar without events is only a date picker. With events, it becomes a planner.

Conceptually:

```
Event =
  - which date?
  - what is it called?
  - optional description
  - optional start/end time
  - optional color for the little dot
```

In code this is the `KalendarEvent` interface. There is a simple implementation called
`BasicKalendarEvent`.

On the day cell, Kalendar draws **up to three small dots** under the day number so the user can
see “this day is busy” without reading every title.

---

## 8. Selection modes — what a tap means

Not every app wants the same interaction.

| Mode | Behavior | Example product |
|------|----------|-----------------|
| **Single** | Only one day selected | “Pick your birthday” |
| **Multiple** | Tap to add/remove many days | “Pick non-working days” |
| **Range** | First tap = start, second = end | “Check-in and check-out” |
| **NoOp** | Taps do nothing special | Display-only calendar |

Range mode is careful: if the user taps the later day first, the library **swaps** start and end
so start is always the earlier date. That saves the app from buggy hotel bookings.

---

## 9. Configuration — one settings bag

Instead of 30 separate function parameters, Kalendar puts options in **`KalendarConfig`**.

Examples of what you can control:

- Should week labels (`Mon Tue …`) show?
- Should navigation arrows show?
- Does the week start on Monday or Sunday?
- What is the earliest / latest date the user may navigate to?
- Which dates are disabled (gray, not tappable)?
- What colors and text styles to use?
- Callback when the **visible range** changes (useful to load events from a server only for
  the months the user is looking at)

**Student takeaway:** good library APIs group related settings into one object so the main function
stays readable.

---

## 10. How the UI is layered (architecture)

When you see a full month (Oceanic), the stack looks like this:

```
Kalendar(...)                      // public front door
   └── KalendarOceanic(...)        // month + arrow navigation
         ├── KalendarHeader        // "March 2026" + arrows
         └── KalendarScaffold      // 7 columns: labels + grid
               └── KalendarDay     // one cell per date
```

### Why split like this?

| Layer | Responsibility |
|-------|----------------|
| Front door | Easy API for app developers |
| Type (Oceanic, …) | Month vs week vs year **behavior** |
| Scaffold | Always “7 columns of dates” structure |
| Day cell | How **one date** looks and feels when selected |

This is **separation of concerns**: each piece does one job. First-year engineering idea: big
problems become small problems glued carefully.

### Why not one giant file?

A 2000-line file that does everything is hard to test and easy to break.  
Smaller files with clear jobs are easier for teams (and for you next semester).

---

## 11. A month grid — the core math (no scary formula)

A month view is a **table with 7 columns** (days of week).

Example for a month that starts on a Wednesday:

```
Mo Tu We Th Fr Sa Su
       1  2  3  4  5
 6  7  8  9 10 11 12
...
```

Empty cells at the start/end are often filled with days from the **previous** or **next** month
(padding dates) so the grid stays a neat rectangle. Those padding days are usually drawn dimmer
and may not be selectable the same way.

Kalendar also lets you choose the **first day of the week** (Monday is the default in config).

That is why calendar code talks about:

- `LocalDate` — a calendar date without a timezone clock time
- `DayOfWeek` — Monday … Sunday
- lists of dates for the current visible period

---

## 12. Programmatic control — the controller

Sometimes the **app** must move the calendar without a user swipe, for example a button:

> “Jump to Christmas”

```kotlin
val controller = rememberKalendarController()

// later:
controller.scrollToDate(LocalDate(2026, 12, 25))
```

Internally, the visible calendar **attaches** a small “how to scroll me” function when it appears
on screen, and **detaches** it when it leaves. That way the controller does not talk to a dead UI.

---

## 13. The sync module (optional chapter)

UI alone does not know about Google Calendar or Apple Calendar.

`kalendar-sync` adds:

| Capability | Meaning |
|------------|---------|
| Fetch events | Read from the device calendar in a date range |
| Insert / update / delete | Write to the device calendar |
| ICS import/export | Read/write the standard `.ics` text format |
| Recurrence expand | Turn “every Monday” rules into concrete dates |

### Platform reality (important lesson)

| Platform | Device calendar support |
|----------|-------------------------|
| Android | Yes (system Calendar provider) |
| iOS | Yes (EventKit) |
| Desktop / Web | Mostly **not** — use ICS files instead |

Good multiplatform design: **share the interface**, provide **real implementations where possible**,
and return **“not supported”** elsewhere instead of crashing.

---

## 14. How the project is built and checked

### Gradle

Gradle is the **build system**. It:

- Compiles Kotlin
- Downloads dependencies
- Runs tests
- Packages libraries for Maven Central (where other projects download them)

Common mental model:

```
./gradlew test     → run automated tests
./gradlew detekt   → static analysis (style / bug smells)
./gradlew assemble → build outputs
```

### Continuous Integration (CI)

On GitHub, workflows under `.github/workflows/` run automatically on pushes and pull requests.
Typical checks:

1. **Detekt** — code quality rules
2. **Unit tests** on the desktop JVM target
3. **Compile** for desktop, Wasm, and Android

So a change that breaks the calendar is more likely to be caught **before** users download a bad
version.

### Automated tests (unit tests)

Tests are small programs that check logic without opening a phone UI, for example:

- “Winter 2026 should include Dec 2026 + Jan/Feb 2027”
- “Range selection should swap inverted dates”
- “ICS parser should read a simple event”

They live under `src/commonTest/` in each module.

---

## 15. How versions and publishing work

Libraries on the internet need **version numbers** so apps can choose a stable release.

This repo uses **Maven coordinates** roughly like:

```
com.himanshoe:kalendar:2.0.0-RC1
com.himanshoe:kalendar-foundation:1.0.0
```

Meaning:

- **Group** `com.himanshoe` — organization / author namespace
- **Artifact** `kalendar` — product name
- **Version** `2.0.0-RC1` — release label (`RC` = release candidate, almost final)

`kalendar` and `kalendar-foundation` can version **independently**. Foundation may stay `1.0.0`
while the main library adds Season in a new release.

---

## 16. A guided path if you want to explore the code

Do this in order (1–2 hours for a first pass):

### Step 1 — Run the demo in your mind

Open:

```
sample/src/commonMain/kotlin/com/himanshoe/sample/App.kt
```

See how little code is needed to show a calendar. That is the **consumer** view.

### Step 2 — Public front door

Open:

```
kalendar/src/commonMain/kotlin/com/himanshoe/kalendar/Kalendar.kt
kalendar/src/commonMain/kotlin/com/himanshoe/kalendar/KalendarType.kt
```

Notice the `when (type)` switch. That is the router between styles.

### Step 3 — One simple style

Open:

```
kalendar/.../KalendarOceanic.kt
```

Trace:

1. State for current month  
2. Header arrows change the month  
3. Scaffold lays out days  
4. Day click updates selection  

### Step 4 — Shared bricks

Open:

```
kalendar-foundation/.../KalendarScaffold.kt
kalendar-foundation/.../KalendarDay.kt
kalendar-foundation/.../OnDaySelectionAction.kt
kalendar-foundation/.../KalendarEvent.kt
kalendar-foundation/.../config/KalendarKonfig.kt   // KalendarConfig lives here
```

### Step 5 — A harder style (optional)

Open `KalendarSeason.kt` or `KalendarYearly.kt`. Same ideas, more period math.

### Step 6 — Docs for users of the library

Browse other files in `doc/` (`Oceanic.md`, `Config.md`, …). Those are written for app developers
using the library, not for studying internals.

---

## 17. Design lessons this project teaches (exam-friendly list)

1. **Reuse over rewrite** — libraries exist so teams do not rebuild calendars forever.
2. **Layering** — foundation vs product vs sample keeps code organized.
3. **Single entry point** — one `Kalendar()` function reduces learning cost.
4. **Strategy by type** — different layouts share infrastructure (like Strategy pattern, informally).
5. **Configuration objects** — group settings instead of endless parameters.
6. **Callbacks for interaction** — library draws; host app decides business logic.
7. **Multiplatform thinking** — share what you can; isolate platform differences.
8. **Test the pure logic** — date math and selection rules are perfect unit-test targets.
9. **Optional modules** — sync is separate so UI-only apps stay lightweight.
10. **Docs matter** — README + `doc/` are part of the product for library authors.

---

## 18. What this project is *not* (avoid confusion)

| It is not… | Why that matters |
|------------|------------------|
| A full Google Calendar clone | No accounts, no cloud sync product, no email invites product |
| A backend server | All code runs in the client app |
| Only an Android project | It is multiplatform Compose |
| Magical AI | It is ordinary engineering: UI + dates + careful APIs |

---

## 19. Tiny glossary

| Term | Short meaning |
|------|----------------|
| **Composable** | Kotlin function that declares UI |
| **State** | Data that, when changed, causes the UI to refresh |
| **Sealed interface / class** | A closed set of subtypes the compiler knows completely |
| **Expect / actual** | KMP pattern: declare API once, implement per platform |
| **ICS** | Text file format for calendar events |
| **RRULE** | Recurrence rule (“every week on Monday”) |
| **Detekt** | Kotlin static analysis tool |
| **Maven Central** | Public warehouse of libraries |
| **minSdk** | Oldest Android version the library supports |

---

## 20. Suggested homework (no submission required)

1. Draw the module diagram from Section 2 on paper from memory.
2. Explain Oceanic vs Firey in one sentence each.
3. Invent a product and pick a Kalendar type + selection mode for it.
4. Read `OnDaySelectionAction` and write which mode you would use for a hotel checkout flow.
5. Open `KalendarSeasonTest` and read one test name — guess what it asserts before reading the body.

If you can do those five, you already understand the **shape** of this codebase.

---

## 21. Where to go next in *this* repo

| Goal | Start here |
|------|------------|
| Install in an app | Root `README.md` |
| Configure colors / bounds | `doc/Config.md` |
| Use a specific layout | `doc/Oceanic.md`, `Firey.md`, … |
| Understand Season | `doc/Season.md` |
| See a live demo | `sample/.../App.kt` |
| Contribute code style rules | `CONTRIBUTING.md`, `detekt.yml` |

---

## Final pep talk

You do not need to understand every line on day one.

A professional library is just:

1. Clear **problem** (calendar UI)
2. Clear **API** (what callers write)
3. Careful **internals** (grids, dates, selection)
4. **Tests and CI** so changes stay safe
5. **Docs** so strangers can use it

Kalendar is a good teaching specimen because those five pieces are all visible in one repo.

Welcome to real software structure. When something feels large, zoom out to modules, then zoom in
to one type file, then to one day cell. That zoom habit will carry you through every engineering
course that follows.

---

*Document purpose: teaching overview for first-year engineers. Not a substitute for the API
reference in `doc/*.md`. Based on the Kalendar multiplatform codebase structure.*
