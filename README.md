# Burnout Detector

**Passive Mobile-Based Mental Fatigue Detection Using Typing and Interaction Patterns**

BSc thesis project, University of Debrecen
Student: Sunderiya Uuganbayar (WCD2M3) · Supervisor: Biro Piroska

---

## About the project

Burnout Detector is a native Android application (Kotlin) that passively observes *how* a user interacts with their phone, never *what* they type, to look for early behavioural signs of mental fatigue.

Using Android's `AccessibilityService`, the app derives interaction metadata such as typing speed, backspace (correction) frequency, scroll variability and app-switching frequency. These features are aggregated into 5-minute windows and compared against a personalised baseline learned during an initial observation period. A lightweight, rule-based anomaly detector flags deviations, and its output is evaluated against participants' self-reported fatigue ratings.

The app is an exploratory fatigue-awareness tool, **not** a clinical or diagnostic system.

## Privacy by design

- **No typed content is stored.** Text from accessibility events is reduced to counts and timings inside the service and discarded immediately; it never reaches the database.
- **Local-only storage.** All data stays on the device in a Room (SQLite) database.
- **User control.** Data is exported only when the user explicitly chooses to, and can be deleted at any time.
- **Informed consent.** The app explains what is collected before monitoring starts.

Details: [`thesis/month-2/privacy-design.md`](thesis/month-2/privacy-design.md)

## Progress

| Month | Focus | Status | Deliverables |
|---|---|---|---|
| 1 | Literature review and problem definition | In revision | [Literature review](thesis/month-1/literature-review.md) · [Problem statement](thesis/month-1/problem-statement.md) · [Research questions](thesis/month-1/research-questions.md) |
| 2 | System design and logging prototype | In progress | [Architecture](thesis/month-2/) · [Database design](thesis/month-2/database-design.md) · [Privacy design](thesis/month-2/privacy-design.md) · [App](android-app/) |
| 3 | Data collection and feature extraction | Planned | [Feature extraction](thesis/month-3/feature-extraction.md) · [Testing report](thesis/month-3/testing-report.md) |
| 4 | Baseline modelling and detection logic | Planned | |
| 5 | User study and data collection | Planned | |
| 6 | Data analysis and validation | Planned | |
| 7–8 | Thesis writing and finalisation | Planned | |

The full working plan: [`thesis/research-foundation.md`](thesis/research-foundation.md)

## Repository structure

```
BurnoutDetector/
├── android-app/          Android Studio project (Kotlin, Room, AccessibilityService)
├── thesis/
│   ├── month-1/          Literature review, problem statement, research questions
│   ├── month-2/          Architecture, database design, privacy design
│   ├── month-3/          Feature extraction, device testing report
│   └── research-foundation.md
└── README.md
```

## Tech stack

- **Language:** Kotlin
- **IDE:** Android Studio
- **Data collection:** Android `AccessibilityService`
- **Storage:** Room (SQLite), on-device only
- **Test devices:** Samsung and Google Pixel (to compare manufacturer-specific accessibility behaviour)

## Building and running

> Build instructions will be added once the logging prototype is committed (Month 2).

Note for testers: on Android 13 and later, apps installed outside the Play Store must be granted **"Allow restricted settings"** (App info → ⋮ menu) before their accessibility service can be enabled.
