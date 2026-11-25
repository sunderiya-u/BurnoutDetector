# Burnout Detector — System Design

## 1. System Overview

Burnout Detector is a native Android application designed to investigate the feasibility of detecting early signs of short-term mental fatigue through passive smartphone interaction monitoring.

The application is developed using Kotlin and Android Studio (minimum SDK: API 28, Android 9). It uses Android's AccessibilityService API to observe interaction events and extract behavioral metadata: typing speed, deletion-based correction frequency, scroll gestures, and application switching frequency.

The collected information is processed locally on the device and aggregated into five-minute time windows. The system will establish a personalized behavioral baseline and use lightweight rule-based anomaly detection to identify deviations from the user's typical interaction patterns. These deviations are evaluated against fatigue ratings that the user reports in the application.

Privacy is a central design requirement. Android delivers event text to the accessibility service, but the application never reads, logs, or stores it: only the event type, timestamp, and character counts are extracted. Passwords, message contents, and screenshots are never collected. Behavioral measurements are stored locally using Room, an abstraction layer over SQLite, and are never transmitted automatically.

The application provides users with information about the monitoring process, controls for stopping monitoring, and options for deleting or explicitly exporting their data.

The system is intended as an exploratory research prototype rather than a medical diagnostic tool. Its effectiveness will be evaluated through technical testing on multiple devices and comparison with self-reported mental fatigue measurements.

## 2. Functional Requirements

Functional requirements describe the operations that the Burnout Detector application must support. Priorities follow the MoSCoW scheme (Must, Should, Could). The last column links each requirement to the research questions it supports.

| ID | Requirement | Description | Priority | Related RQ |
|---|---|---|---|---|
| FR01 | User information and consent | The application must explain what is and is not collected before monitoring starts, and the user must explicitly enable the accessibility service in the system settings. | Must | — |
| FR02 | Interaction monitoring | The application must receive text-change, scroll, and window-change accessibility events while monitoring is enabled. | Must | RQ1 |
| FR03 | Sensitive context exclusion | The application must ignore events flagged as password fields by `isPassword()`, events from its own user interface, and text-change events from system sign-in and credential components. Password detection is best-effort: fields that are not flagged as passwords cannot be recognized (see `testing-report.md`, Section 8.6). | Must | RQ1 |
| FR04a | Typing speed | The application must derive typing speed from the timing of text-change events and their net character change (`addedCount − removedCount`), without reading textual content. | Must | RQ1, RQ2 |
| FR04b | Correction frequency | The application must detect deletions (net change of −1) and compute a deletion-based correction ratio: deletions ÷ (typed characters + deletions). Multi-character replacements (e.g., autocorrect) are counted separately and are not part of the ratio. | Must | RQ1, RQ2 |
| FR05 | Scroll monitoring | The application must group scroll events into scroll gestures and record their number and duration, and the scroll distance where an application reports it. | Should | RQ1, RQ2 |
| FR06 | Application switching | The application must count switches to a different application that stays in the foreground for at least about one second, excluding the launcher, keyboard, system UI, and system overlays. Package names are not stored. | Should | RQ1, RQ2 |
| FR07 | Local storage | The application must store behavioral measurements in a local Room database. | Must | — |
| FR08 | Feature aggregation | The application must aggregate collected measurements into fixed, non-overlapping, clock-aligned five-minute windows. | Must | RQ2 |
| FR09 | Fatigue self-report | The application must prompt the user several times per day to rate their current fatigue on a standardized scale and store each rating with a timestamp. | Must | RQ2, RQ3 |
| FR10 | Monitoring status | The application must show whether monitoring is active and how much data has been collected. | Should | — |
| FR11 | Monitoring controls | Users must be able to stop and restart monitoring. | Must | — |
| FR12 | Data deletion | Users must be able to delete all locally stored data. | Must | — |
| FR13 | Data export | Users must be able to explicitly export their stored measurements (e.g., as a CSV file). | Must | RQ2, RQ3 |
| FR14 | Personalized baseline | The application must establish an individual behavioral baseline after an initial observation period. | Must | RQ3 |
| FR15 | Anomaly detection | The application must compare recent windows with the baseline using lightweight rules. | Must | RQ3 |
| FR16 | Fatigue feedback | The application could display fatigue-related indicators and optional break suggestions. | Could | — |

**Implementation timeline (status at the end of Month 2):**

| Phase | Requirements | Status |
|---|---|---|
| Month 2 | FR01 consent, FR02 event reception, FR11 monitoring controls | Implemented and tested on one device |
| Month 2 | FR03 sensitive context exclusion | Partly implemented: password flag, own application, system and keyboard windows. Login-context exclusions are added in Month 3 |
| Month 2 | FR10 monitoring status | Partly implemented: shows whether monitoring is requested or paused. Data-collection status is added in Month 3 |
| Month 3 | FR04a, FR04b, FR05, FR06, FR07, FR08, FR12 | Planned: feature extraction, in-memory aggregation, Room storage, and data deletion |
| Month 4 | FR09, FR13, FR14, FR15, FR16 | Planned: fatigue prompts, export, baseline, anomaly detection, and feedback (export and prompts must be completed before the user study in Month 5) |

**Feasibility note for FR04a–b.** Android accessibility events do not expose individual keypresses. They do report how many characters were added and removed in each text change. Device testing on a Samsung Galaxy S21 Ultra showed two reporting patterns, single-character changes and whole-word replacement, which are both handled by classifying events by their net change (see `testing-report.md`, Section 8.2). Whether this holds for other manufacturers and keyboard applications (e.g., Gboard) is investigated under RQ1 on a second device.

## 3. Non-Functional Requirements

Non-functional requirements describe the quality, privacy, reliability, and performance characteristics of the system. Each is stated so that it can be verified.

### NFR01 — Privacy
The application must never read, log, or store the text content of accessibility events. Only event type, timestamp, character counts, and (where available) scroll values are extracted. Android may include text in the events it delivers, including text from password fields that are not flagged as such, so this requirement is enforced by the application's code. Events from fields flagged as passwords are ignored entirely (FR03). Per-event values and package names must not be written to logs in builds used with participants.
*Verification:* code review of the event-processing and logging code; inspection of the database and exported files.

### NFR02 — Security
All data must be stored in the application's private storage and must not be transmitted over the network. The application does not request the `INTERNET` permission.
*Verification:* inspection of `AndroidManifest.xml`.

### NFR03 — Performance
Event handling must not block the main thread. Database writes must run on a background thread. Battery consumption attributed to the application should stay below approximately 3% per day under normal use.
*Verification:* code review; Android battery usage statistics during device testing.

### NFR04 — Reliability
Events with missing or unexpected fields must be skipped without crashing and counted in a diagnostics counter, so that data loss is visible rather than silent.
*Verification:* diagnostics counter in the status screen; device testing.

### NFR05 — Usability
The consent and status screens must explain data collection in plain language, and stopping monitoring or deleting data must take no more than two taps from the main screen.
*Verification:* walkthrough with test participants.

### NFR06 — Compatibility
The application must run on Android 9 (API 28) and later, and must be tested on at least two devices from different manufacturers (e.g., Samsung and Google Pixel) to compare event availability.
*Verification:* device testing report.

### NFR07 — Maintainability
Event collection, event processing, database access, and user interface logic must be implemented as separate components (see Section 4).
*Verification:* project structure.

### NFR08 — Transparency
The application must state that its fatigue-related outputs are experimental indicators and not a clinical diagnosis.
*Verification:* consent and feedback screens.

### NFR09 — Data retention
Individual interaction events must never be written to storage; only aggregated five-minute windows are stored. All data must be deletable by the user at any time (FR12). Participants' data is retained only for the duration of the study.
*Verification:* inspection of the database schema and contents.

## 4. Architecture

The application is organized as a pipeline of layers. Each layer has one responsibility, which supports maintainability (NFR07) and makes the privacy boundary explicit. The diagram is exported from [`architecture.drawio`](architecture.drawio); solid boxes are implemented, dashed boxes are planned.

![Architecture](architecture.png)

| Layer | Component | Status | Responsibility |
|---|---|---|---|
| 1. Android system | — | — | Emits accessibility events for text changes, scrolling, and window changes |
| 2. Consent and control | `ConsentPreferences` (Jetpack DataStore), `MonitoringGate` | Implemented | Stores consent and the monitoring preference; closes monitoring immediately on pause or withdrawal |
| 3. Event collection | `BurnoutAccessibilityService` | Implemented | Receives events and processes them only while consent is given and monitoring is enabled |
| 4. Privacy filtering | `PrivacyEventFilter` | Implemented | Rejects events flagged as password fields, events from the application itself, system and keyboard windows, and unsupported event types |
| 5. Classification and aggregation | Event classifier and in-memory window accumulator | Planned (Month 3) | Classifies events by net change, groups scroll gestures, detects application switches, and keeps running totals in memory. **Privacy boundary:** only numeric window totals leave this layer |
| 6. Storage | Room database (`AppDatabase`, DAOs) | Planned (Month 3) | Stores aggregated five-minute windows and fatigue prompts on the device. Individual events are never stored |
| 7. Presentation | Compose screens | Partly implemented | Consent, monitoring controls, and withdrawal are implemented; fatigue prompts, data status, export, and deletion are planned |

## 5. Data Flow

1. The user types, scrolls, or switches apps.
2. Android sends an `AccessibilityEvent` to `BurnoutAccessibilityService`.
3. The service checks the monitoring gate, and `PrivacyEventFilter` checks the event type and source. Events flagged as password fields, events from the application itself, and events from system, keyboard, sign-in, and credential components are discarded.
4. For accepted events, the classifier determines the event type (typed character, deletion, replacement, scroll, or application switch) and updates running totals **in memory**. Only numeric values are used: the net character change, timestamps, and, where available, scroll distance.
5. Individual events are never written to storage. This protects against cases where a password field is not recognized by Android (see `testing-report.md`, Section 8.6).
6. At the end of each fixed five-minute window, the in-memory totals are converted into an `InteractionWindow` containing the computed features (typing speed, deletion-based correction ratio, scroll gestures, application switches), which is written to the Room database on a background thread. Typing features are stored as `null` if fewer than 20 characters were typed. The totals are then reset (NFR09). Partial and incomplete windows are handled as defined in `database-design.md`, Section 6.
7. Each fatigue prompt is stored as a `FatigueReport` record when it is shown; the rating is added when the participant answers. Answered reports are later linked to the windows from the preceding 30 minutes.

## 6. Database Design

The database schema, entity definitions, and relationships are described in [`database-design.md`](database-design.md).

## 7. Privacy Design

Consent, data minimization, the privacy boundary, retention, export, and deletion are described in [`privacy-design.md`](privacy-design.md).
