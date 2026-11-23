# Burnout Detector — Privacy Design

## 1. Overview

Burnout Detector is a research prototype that investigates whether passive smartphone interaction patterns can provide indicators of short-term mental fatigue.

Because the application uses Android's AccessibilityService API, privacy and informed consent are essential design requirements.

The system follows the principles of data minimization, transparency, local processing, user control, and limited retention.

The application does not attempt to diagnose clinical burnout or any medical condition.

## 2. Data Collection

The application collects only behavioral metadata required for the research.

| Data | Collected | Purpose |
|---|---|---|
| Event timestamps | Yes | Calculate timing and activity patterns |
| Character addition counts | Yes, when available | Estimate typing activity |
| Character deletion counts | Yes, when available | Estimate correction activity |
| Scroll events and available distances | Yes | Measure scrolling patterns |
| Application transitions | Yes, as counts | Measure switching frequency |
| Self-reported fatigue ratings | Yes | Validate behavioral measurements |
| Typed text and message contents | No | Not required |
| Passwords | No | Sensitive information |
| Screenshots | No | Not required |
| Application package names | Read in memory, never stored | Detect application switches and filter the application's own, keyboard, and system windows |
| Personal names and contact details | No | Not required |

Android accessibility events may expose information beyond these measurements. The application never reads event text, and it discards all information unnecessary for feature extraction immediately after processing each event.

## 3. Legal Basis and Data Protection (GDPR)

The study is conducted at the University of Debrecen and is therefore subject to the EU General Data Protection Regulation (GDPR).

Behavioral measurements are stored under a pseudonymous participant code (e.g., `P03`) rather than a name. Pseudonymized data is still personal data under the GDPR, because the code can be linked back to a participant. To limit this risk:

- The list linking participant codes to names is kept separately from the research data, is accessible only to the researcher, and is destroyed after the study period.
- Research data shared with the supervisor or included in the thesis contains only participant codes or aggregated results.

Participants will provide informed consent to participate in the research. The appropriate GDPR legal basis under Article 6, and any applicable condition under Article 9 for processing health-related data, will be confirmed with the thesis supervisor and the university's data protection officer before external participants are recruited.

## 4. Informed Consent

### 4.1. Participation Criteria

Participants must be at least 18 years old and own an Android smartphone running Android 9 (API 28) or later.

### 4.2. Consent Process

Before monitoring begins, the application displays an information and consent screen explaining:

- The purpose of the research.
- The behavioral information collected.
- The information explicitly excluded.
- How the data is stored and processed.
- The experimental nature of fatigue indicators.
- The participant's ability to stop monitoring and delete their data.
- How exported research data may be used.

The participant must explicitly accept the information screen before being directed to Android's accessibility settings.

Consent is recorded locally using a timestamp in Jetpack DataStore.

Enabling the accessibility service is a separate Android system action and does not replace research consent.

Participation is voluntary, and withdrawing must not disadvantage the participant.

The full participant information sheet and consent form are included as an appendix to the thesis.

## 5. AccessibilityService Privacy Boundary

Privacy is enforced in two layers: first by the Android system, through the service configuration, and then by the application's own `EventProcessor`.

### 5.1. System-Level Restrictions

The accessibility service configuration file (`accessibility_service_config.xml`) restricts what Android delivers to the application:

- **`canRetrieveWindowContent="false"`**: Android prevents the service from reading the content of the screen (the window content tree). The service receives only the events themselves. This enforces the privacy boundary at the system level rather than relying solely on application code.
- **Restricted event types**: only `typeViewTextChanged`, `typeViewScrolled`, and `typeWindowStateChanged` are requested, so Android does not deliver any other event types.

Disabling window-content retrieval does not remove textual information from accessibility events. Therefore, the `EventProcessor` must avoid accessing event text (including `event.text` and `beforeText`) and discard all unnecessary event fields.

Password-field detection remains possible under these restrictions, because `AccessibilityEvent.isPassword()` is available on the event itself.

### 5.2. Application-Level Processing

The `EventProcessor` acts as the privacy boundary between Android events and the application's database.

It must:

1. Accept only the event types needed for the research.
2. Exclude events from password fields, identified with `AccessibilityEvent.isPassword()`.
3. Exclude events originating from Burnout Detector itself.
4. Ignore irrelevant system and keyboard windows.
5. Extract only timestamps, event categories, and permitted numeric measurements.
6. Never read or log event text.
7. Discard the original accessibility event after processing.

No raw text, screenshots, or application package names are written to the database.

Password-field detection and other exclusions are best-effort protections, not guarantees that every sensitive context can be identified. For example, a field that holds sensitive information but is not marked as a password field by its application cannot be recognized. This limitation must be included in the participant information and evaluated during testing.

## 6. Local Storage and Security

Behavioral measurements and fatigue reports are stored using Room in the application's private local storage.

The application does not request the `INTERNET` permission. As a result, Android prevents it from transmitting any data over the network.

The database is accessible only within the application's normal Android application sandbox, subject to device security.

Private storage alone does not guarantee encryption at rest. Database encryption is outside the initial prototype scope unless later required by the study's security review.

Research data is excluded from Android's automatic backup:

- `android:allowBackup="false"` is set in `AndroidManifest.xml`.
- A `dataExtractionRules` configuration excludes all application data from cloud backup and device-to-device transfer (required on Android 12 and later).

## 7. Distribution

Google Play applies additional policy requirements to applications using AccessibilityService. To simplify distribution of this research prototype, Burnout Detector will be distributed directly to approved study participants rather than through Google Play. Participants install the application as an APK file provided by the researcher.

On Android 13 and later, applications installed this way must be granted "Allow restricted settings" (App info → menu) before their accessibility service can be enabled. The participant instructions explain this step.

## 8. Data Retention

Temporary `InteractionEvent` records are retained only until their five-minute window has been successfully aggregated.

After successful aggregation, the temporary records are deleted within the same Room transaction used to store the corresponding `InteractionWindow`.

Incomplete windows and interrupted processing are handled by the recovery mechanism described in [`database-design.md`](database-design.md).

Aggregated windows and fatigue reports are retained only for the study period, subject to the approved research protocol.

Participants may delete all locally stored research measurements at any time.

## 9. Monitoring Controls

The application provides a main screen showing the current monitoring status.

Users can pause or resume collection through the application.

Pausing monitoring must stop processing and storing new behavioral events, even if Android's accessibility service remains enabled.

The interface also explains how to disable the accessibility service entirely in Android settings.

## 10. Data Export

Users can explicitly export their `InteractionWindow` and `FatigueReport` measurements as CSV files.

Exports include a pseudonymous participant code instead of the participant's name.

Temporary `InteractionEvent` records are not exported.

Export requires a deliberate user action and uses Android's document creation interface, so the participant can choose where the file is saved.

The application must explain that exported files may no longer be protected by the application's private storage.

## 11. Data Deletion and Withdrawal

The application provides an option to delete all locally stored research data.

Deletion includes temporary interaction events, aggregated interaction windows, fatigue reports, and stored baseline or anomaly information.

The application also clears participant-specific settings and consent records as appropriate.

Deletion must be confirmed by the participant to avoid accidental data loss.

Deleting local data does not automatically delete copies previously exported or transferred to researchers. The study information must explain how participants can request deletion of any externally held data, subject to the approved protocol.

## 12. Research Ethics and Limitations

Burnout Detector is intended for exploratory research, not clinical diagnosis.

Participants must be informed that:

- Accessibility-based monitoring may vary across Android devices and keyboards.
- Some measurements may be incomplete or inaccurate.
- The application does not evaluate the meaning or content of typed messages.
- Behavioral changes cannot establish mental fatigue on their own.
- Participation is voluntary and monitoring can be stopped.

Before collecting data from external participants, the study protocol, consent materials, data handling arrangements, and any required university ethics approval must be confirmed with the thesis supervisor.

## 13. Verification

| Requirement | Verification method |
|---|---|
| No text collection | Review `EventProcessor` and logging code |
| System-level content restriction | Inspect `accessibility_service_config.xml` (`canRetrieveWindowContent="false"`, event types) |
| Password-field exclusion | Test password inputs on supported devices |
| Local-only processing | Confirm the `INTERNET` permission is absent from the manifest |
| Consent before monitoring | Test onboarding flow |
| Monitoring pause | Verify that no new events are stored |
| Temporary-event deletion | Inspect database after aggregation |
| User-controlled export | Test CSV export |
| Complete local deletion | Inspect database and DataStore after deletion |
| No automatic backup | Inspect `allowBackup` and `dataExtractionRules` configuration |

In summary, the design minimizes data collection while preserving the measurements the research requires, and each safeguard above is verified during implementation and device testing.s