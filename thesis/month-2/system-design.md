# Burnout Detector — System Design

## 1. System Overview

Burnout Detector is a native Android application designed to investigate the feasibility of detecting early signs of short-term mental fatigue through passive smartphone interaction monitoring.

The application is developed using Kotlin and Android Studio. It uses Android's AccessibilityService API to observe permitted interaction events and extract behavioral metadata, such as scrolling activity, application switching frequency, and typing-related indicators where technically available.

The collected information is processed locally on the device and aggregated into five-minute time windows. The system will establish a personalized behavioral baseline and use lightweight rule-based anomaly detection to identify deviations from the user's typical interaction patterns.

Privacy is a central design requirement. The application will not intentionally collect or store typed text, passwords, message contents, or screenshots. Behavioral measurements will be stored locally using Room, an abstraction layer over SQLite.

The application will provide users with information about the monitoring process, controls for stopping monitoring, and options for deleting or explicitly exporting their data.

The system is intended as an exploratory research prototype rather than a medical diagnostic tool. Its effectiveness will be evaluated through technical testing and comparisons with self-reported mental fatigue measurements.

## 2. Functional Requirements

Functional requirements describe the operations that the Burnout Detector application must support.

| ID | Requirement | Description |
|---|---|---|
| FR01 | User information and consent | The application must explain its monitoring purpose and require the user to explicitly enable the accessibility service. |
| FR02 | Interaction monitoring | The application must receive supported Android accessibility events while monitoring is enabled. |
| FR03 | Scroll monitoring | The application must count supported scrolling events. |
| FR04 | Application switching | The application must identify application transitions where the necessary event metadata is available. |
| FR05 | Typing-related monitoring | The application should extract feasible typing-related indicators without recording textual content. |
| FR06 | Feature aggregation | The application must aggregate collected measurements into five-minute time windows. |
| FR07 | Local storage | The application must store behavioral measurements in a local Room database. |
| FR08 | Monitoring controls | Users must be able to stop monitoring. |
| FR09 | Data deletion | Users must be able to delete their locally stored behavioral data. |
| FR10 | Data export | Users must be able to explicitly export their stored measurements. |
| FR11 | Personalized baseline | The application will establish an individual behavioral baseline after an observation period. |
| FR12 | Anomaly detection | The application will compare recent measurements with the baseline using lightweight rules. |
| FR13 | Fatigue feedback | The application may display fatigue-related indicators and optional break suggestions. |

The implementation of FR11–FR13 is planned for a later development phase. The feasibility of FR05 must be established through early testing because Android accessibility events do not guarantee access to individual keypresses or backspace operations.
## 3. Non-Functional Requirements

Non-functional requirements describe the quality, privacy, reliability, and performance characteristics of the system.

### NFR01 — Privacy

The application must not intentionally collect or retain typed text, passwords, message contents, or screenshots. Only the behavioral metadata required for the research should be processed and stored.

### NFR02 — Security

Collected measurements must be stored in the application's private local storage. Data must not be automatically transmitted to external servers.

### NFR03 — Performance

Event processing must be lightweight and avoid unnecessary computational overhead, excessive battery consumption, and blocking of the main application thread.

### NFR04 — Reliability

The system must handle missing, duplicated, or unsupported accessibility events without crashing or producing misleading measurements.

### NFR05 — Usability

The application must provide understandable monitoring information and accessible controls for managing data collection.

### NFR06 — Compatibility

The application should be tested on at least two Android devices to evaluate differences in event availability and behavior.

### NFR07 — Maintainability

The implementation should separate event collection, feature processing, database operations, and user interface logic into clearly defined components.

### NFR08 — Transparency

The application must explain that its fatigue-related outputs are experimental indicators and do not constitute a clinical diagnosis.