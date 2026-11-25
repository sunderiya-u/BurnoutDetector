# Burnout Detector

**Passive Mobile-Based Mental Fatigue Detection Using Typing and Interaction Patterns**

BSc thesis project, University of Debrecen

**Student:** Sunderiya Uuganbayar
**Supervisor:** Biro Piroska

---

## About the Project

Burnout Detector is a native Android research application developed in Kotlin. The project investigates whether smartphone interaction patterns can provide early indicators of mental fatigue without collecting the actual content of user interactions.

The proposed system uses Android's AccessibilityService to derive behavioral metadata, including:

- Typing-related speed and timing indicators.
- Backspace and correction frequency.
- Scroll variability.
- Application-switching frequency.

The planned behavioral features will be aggregated into five-minute windows and compared against a personalized baseline established during an initial observation period.

A lightweight, rule-based anomaly detection mechanism will identify behavioral deviations. Its output will be evaluated against participants' self-reported fatigue ratings.

The application is an exploratory fatigue-awareness research tool, not a clinical or diagnostic system.

## Current Development Status

**Current milestone: Month 2 - Completed at prototype level**

The Android application currently implements:

- Research consent interface.
- Persistent consent management using Jetpack DataStore.
- Monitoring activation and pause controls.
- Consent withdrawal.
- AccessibilityService prototype.
- Privacy event filtering.
- Immediate monitoring permission control.
- Generation-based invalidation of outdated activation requests.
- Safe application process restart behavior.
- Android backup restrictions.
- Preference error handling.

Behavioral metric extraction and Room database integration are planned for Month 3 and are not yet implemented.

## Privacy by Design

### Implemented Safeguards

- Explicit informed consent before monitoring.
- Monitoring disabled by default.
- Immediate monitoring permission revocation.
- Manual monitoring reactivation after process restart.
- Rejection of password-flagged accessibility events.
- Exclusion of selected system and keyboard applications.
- Restricted accessibility event categories.
- No access to actual typed text in the implemented service.
- No screenshot collection.
- No persistent storage of application package names.
- No declared Internet permission.
- Android backup restrictions.

The current prototype stores only consent and monitoring preferences. Behavioral metrics are not yet extracted or persisted.

### Planned Safeguards

- Local-only behavioral data storage using Room.
- Sanitized numerical feature extraction.
- User-controlled behavioral data export.
- User-controlled behavioral data deletion.
- Monitoring-session-aware data processing.

The implemented safeguards represent an initial privacy architecture rather than a complete privacy or regulatory compliance assessment.

For further details, see the [Privacy Design](thesis/month-2/privacy-design.md).

## System Architecture

The current prototype separates user interface logic, persistent preference management, monitoring permission control, accessibility event observation, and privacy filtering.

| Component | Responsibility |
|---|---|
| MainActivity | Consent interface and monitoring dashboard |
| ConsentPreferences | Persistent consent and monitoring preferences |
| MonitoringGate | Immediate monitoring permission control |
| BurnoutAccessibilityService | Accessibility event observation |
| PrivacyEventFilter | Event eligibility and privacy filtering |

Architecture documentation:

- [System Design](thesis/month-2/system-design.md)
- [Editable Architecture Diagram](thesis/month-2/architecture.drawio)
- [Database Design](thesis/month-2/database-design.md)
- [Implementation Report](thesis/month-2/implementation-report.md)

## Testing and Verification

### Month 2 Test Results

| Testing category | Passed | Failed | Inconclusive |
|---|---:|---:|---:|
| Automated unit tests | 20 | 0 | 0 |
| Physical device regression tests | 9 | 0 | 1 |
| Total | 29 | 0 | 1 |

Additional verification included:

- AccessibilityService connection testing.
- Monitoring permission verification using Logcat.
- Application process restart testing.
- Physical device reboot testing.
- Android manifest inspection.
- Installed application backup flag inspection.
- Android Backup Manager rejection testing.

Physical device testing was performed on a Samsung Galaxy S21 Ultra (SM-G998N) running Android 15.

Withdrawal during a pending activation operation remains inconclusive and requires controlled integration testing.

Further testing of storage failure recovery, activity lifecycle behavior, sensitive event handling, and device compatibility is required before research participant deployment.

Full details: [Month 2 Testing Report](thesis/month-2/testing-report.md).

## Project Progress

| Month | Focus | Status |
|---|---|---|
| 1 | Literature review and problem definition | In revision |
| 2 | System design and monitoring prototype | Completed at prototype level |
| 3 | Data collection and feature extraction | Next milestone |
| 4 | Baseline modelling and detection logic | Planned |
| 5 | User study and data collection | Planned |
| 6 | Data analysis and validation | Planned |
| 7-8 | Thesis writing and finalisation | Planned |

### Month 1 - Research Foundation

- [Literature Review](thesis/month-1/literature-review.md)
- [Problem Statement](thesis/month-1/problem-statement.md)
- [Research Questions](thesis/month-1/research-questions.md)

### Month 2 - System Design and Prototype

- [System Design](thesis/month-2/system-design.md)
- [Architecture Diagram](thesis/month-2/architecture.drawio)
- [Database Design](thesis/month-2/database-design.md)
- [Privacy Design](thesis/month-2/privacy-design.md)
- [Implementation Report](thesis/month-2/implementation-report.md)
- [Testing Report](thesis/month-2/testing-report.md)
- [Completion Summary](thesis/month-2/completion-summary.md)

### Month 3 - Feature Extraction and Data Storage

- [Feature Extraction](thesis/month-3/feature-extraction.md)
- [Testing Report](thesis/month-3/testing-report.md)

Full research plan: [Research Foundation](thesis/research-foundation.md).

## Repository Structure

```text
BurnoutDetector/
|-- android-app/
|   |-- app/
|   |-- gradle/
|   |-- build.gradle.kts
|   |-- settings.gradle.kts
|-- thesis/
|   |-- month-1/
|   |-- month-2/
|   |   |-- architecture.drawio
|   |   |-- database-design.md
|   |   |-- privacy-design.md
|   |   |-- system-design.md
|   |   |-- implementation-report.md
|   |   |-- testing-report.md
|   |   |-- completion-summary.md
|   |-- month-3/
|   |-- research-foundation.md
|-- README.md
```

## Technology Stack

| Technology | Purpose | Status |
|---|---|---|
| Kotlin | Android application development | Implemented |
| Android Studio | Development environment | In use |
| Jetpack Compose | User interface | Implemented |
| Jetpack DataStore | Persistent preferences | Implemented |
| AccessibilityService | Interaction event observation | Prototype implemented |
| Kotlin Coroutines and Flow | Asynchronous processing | Implemented |
| JUnit | Automated unit testing | Implemented |
| Room / SQLite | Local behavioral data storage | Planned |
| Rule-based anomaly detection | Behavioral deviation detection | Planned |
| Git and GitHub | Version control | In use |

## Building and Running

### Prerequisites

- Android Studio.
- Android SDK compatible with the project configuration.
- Compatible Java Development Kit.
- Android device or emulator running Android 9 (API 28) or later.

### Installation

1. Clone this repository.
2. Open the android-app directory in Android Studio.
3. Allow Gradle synchronization to complete.
4. Connect an Android device or start an emulator.
5. Select the app run configuration.
6. Build and run the application.

### Running the Prototype

1. Open Burnout Detector.
2. Read the research consent information.
3. Accept consent to access the dashboard.
4. Enable Burnout Detector Monitoring in Android Accessibility settings.
5. Return to the application.
6. Enable monitoring manually.
7. Use the pause or withdrawal controls to revoke monitoring permission.

Depending on the Android version and installation method, Android may require additional confirmation through the Allow restricted settings option before the accessibility service can be enabled.

Monitoring is paused by default after an application process restart.

### Building from the Terminal

From the android-app directory on Windows:

```powershell
.\gradlew.bat assembleDebug
```

### Running Unit Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

The generated HTML test report is available at:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

## Next Development Milestone

Month 3 will focus on:

- Room database implementation.
- Sanitized behavioral metadata extraction.
- Typing-related feature calculation.
- Scrolling-related feature calculation.
- Application-switch counting.
- Five-minute feature aggregation.
- Monitoring-session-aware data processing.
- Local behavioral data export and deletion.
- Additional integration and physical device testing.

## Research Disclaimer

Burnout Detector is an academic research prototype.

It does not diagnose burnout, mental fatigue, or any medical condition. The proposed behavioral indicators require experimental evaluation and validation.

The current prototype is not yet ready for research participant deployment.

---

University of Debrecen | Faculty of Informatics | BSc Computer Science
