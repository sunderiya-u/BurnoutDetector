# Month 2 — Testing and Verification Report

**Project:** Burnout Detector: Passive Mobile-Based Mental Fatigue Detection Using Typing and Interaction Patterns

**Report date:** November 25, 2026

## 1. Testing Objectives

The objective of Month 2 testing was to verify the functionality, reliability, and initial privacy safeguards of the Android application prototype before implementing behavioral data collection.

Testing focused on:

- Explicit consent management.
- Persistent preference storage.
- Monitoring activation and deactivation.
- Accessibility event filtering.
- Immediate monitoring permission revocation.
- Safe application process restart behavior.
- Android backup protection.
- Consent withdrawal.
- User interface functionality.

## 2. Testing Environment

| Component | Specification |
|---|---|
| Physical device | Samsung Galaxy S21 Ultra (SM-G998N) |
| Operating system | Android 15 |
| Development environment | Android Studio |
| Programming language | Kotlin |
| Testing framework | JUnit |
| Build system | Gradle 9.9.1 |
| Device communication | Android Debug Bridge (ADB) |
| Runtime observation | Android Studio Logcat |
| Build variant | Debug |
| Final testing date | November 25, 2026 |

## 3. Automated Unit Testing

Unit tests were executed using the following Gradle command:

```powershell
.\gradlew.bat testDebugUnitTest
```

### 3.1 Final Unit Test Results

| Test class | Total | Passed | Failed | Skipped |
|---|---:|---:|---:|---:|
| ExampleUnitTest | 1 | 1 | 0 | 0 |
| PrivacyEventFilterTest | 13 | 13 | 0 | 0 |
| MonitoringGateTest | 6 | 6 | 0 | 0 |
| Total | 20 | 20 | 0 | 0 |

Overall success rate: 100%.

Reported test run duration: 0.927 seconds.

The final Gradle test report confirmed that all 20 unit tests completed successfully.

### 3.2 Privacy Event Filter Testing

The PrivacyEventFilterTest class contains 13 unit tests covering the event eligibility logic.

The tested filtering behavior includes monitoring permission checks, password-related event rejection, invalid package names, application exclusions, and supported accessibility event types.

All 13 tests passed.

These tests verify filtering logic using controlled inputs. They do not establish that Android identifies every sensitive interaction correctly in real-world applications.

### 3.3 Monitoring Gate Testing

The MonitoringGateTest class contains six unit tests.

| Test method | Objective | Result |
|---|---|---|
| gateIsClosedAfterReset | Verify that the gate is closed after reset | Passed |
| gateOpensWithCurrentRequest | Verify valid activation | Passed |
| gateClosesImmediately | Verify immediate gate closure | Passed |
| staleEnableRequestCannotReopenGate | Reject outdated activation requests | Passed |
| repeatedPauseKeepsGateClosed | Verify repeated closure safety | Passed |
| newEnableRequestCanReopenGate | Verify valid reactivation | Passed |

All six tests passed.

The results support the correctness of the generation-based request invalidation mechanism under the tested conditions.

These unit tests do not replace full integration testing of concurrent preference updates and consent withdrawal.

## 4. Physical Device Regression Testing

Final regression testing was performed on the Samsung Android device after implementing the Month 2 user interface and privacy improvements.

| ID | Test scenario | Expected behavior | Result |
|---|---|---|---|
| T01 | Updated consent screen | Display planned research metrics and prototype explanation | Passed |
| T02 | Consent validation | Continue remains disabled until consent is selected | Passed |
| T03 | Consent acceptance | Dashboard appears with monitoring paused | Passed |
| T04 | Enable monitoring | Monitoring requested status and allowed=true in Logcat | Passed |
| T05 | Pause monitoring | Monitoring paused status and allowed=false in Logcat | Passed |
| T06 | Withdrawal during activation | Pending activation cannot override consent withdrawal | Inconclusive |
| T07 | Normal withdrawal | Consent screen appears and monitoring is disabled | Passed |
| T08 | Safe process restart | Consent persists but monitoring remains paused | Passed |
| T09 | Withdrawal persistence | Consent remains removed after process restart | Passed |
| T10 | Repeated monitoring toggles | Dashboard and monitoring permission remain consistent | Passed |

### 4.1 Regression Testing Summary

- Total planned tests: 10.
- Passed: 9.
- Failed: 0.
- Inconclusive: 1.

Nine of the ten planned regression scenarios were confirmed as successful.

The inconclusive scenario concerns withdrawal during a pending monitoring activation operation. The required concurrent-operation condition was not conclusively reproduced during manual testing.

### 4.2 Runtime Permission Verification

Android Studio Logcat was used to observe monitoring permission transitions.

The following diagnostic messages were used during verification:

```text
Accessibility service connected
DataStore observer started
DataStore emitted: allowed=true
DataStore emitted: allowed=false
Immediate gate closed: Pause
Immediate gate closed: Withdraw
```

These messages support verification of monitoring permission transitions.

They do not demonstrate behavioral metric extraction or database persistence because those components have not yet been implemented. The interaction events themselves were observed in a separate test (Section 8).

## 5. Application Restart Verification

Application process restart behavior was tested using Android Debug Bridge.

The application was force-stopped and manually reopened.

The expected behavior was that persistent consent remained available while the in-memory monitoring gate returned to its default closed state.

This behavior was successfully verified.

A physical device reboot was also performed during earlier restart verification.

The application required manual monitoring reactivation after restarting.

These results support the intended privacy-by-default process lifecycle behavior.

## 6. Android Backup Protection Verification

### 6.1 Configuration Inspection

The application manifest and backup configuration files were inspected.

The following safeguards were configured:

- android:allowBackup="false"
- Legacy full-backup exclusions.
- Modern cloud-backup exclusions.
- Device-transfer exclusions.

The merged manifest was inspected to confirm that the intended backup attributes were present.

### 6.2 Installed Application Flag Verification

The installed application package was inspected using the Android package manager.

The relevant output was:

```text
flags=[ DEBUGGABLE HAS_CODE ALLOW_CLEAR_USER_DATA TEST_ONLY ]
pkgFlags=[ DEBUGGABLE HAS_CODE ALLOW_CLEAR_USER_DATA TEST_ONLY ]
```

The ALLOW_BACKUP flag was absent.

This confirmed that the installed application did not have the standard Android application backup flag enabled.

### 6.3 Android Backup Manager Test

An explicit backup request was executed:

```text
adb shell bmgr backupnow com.sunderiya.burnoutdetector
```

The device returned:

```text
Running incremental backup for 1 requested packages.
Package com.sunderiya.burnoutdetector with result: Backup is not allowed
Backup finished with result: Success
```

The package-specific result confirmed that the Android Backup Manager rejected the backup request for Burnout Detector.

The final Success message refers to the completion of the Backup Manager operation, not successful backup of the application data.

Result: Passed.

### 6.4 Device-Transfer Limitation

Although device-transfer exclusion rules were configured, physical device-to-device migration was not experimentally tested.

Consequently, the observed backup rejection must not be interpreted as proof that all manufacturer-specific migration mechanisms are blocked.

## 7. Privacy Verification

Source-code and configuration inspection confirmed the following safeguards in the current prototype.

| Privacy requirement | Verification result |
|---|---|
| Explicit consent before monitoring | Verified |
| Monitoring disabled by default | Verified |
| Immediate monitoring gate closure | Verified |
| Password-flagged event rejection | Verified in filtering logic; device testing showed that some password fields are not flagged (Section 8.6) |
| Restricted accessibility event categories | Verified |
| No access to typed text in the implemented service | Verified by source-code inspection |
| No accessibility node content retrieval | Configured |
| No screenshot collection | Verified by source-code inspection |
| No persistent behavioral metric storage | Not implemented |
| No persistent application package-name storage | Verified by source-code inspection |
| No declared Internet permission | Verified |
| Standard Android backup rejection | Verified on the test device |
| Device-transfer exclusions | Configured; physical migration untested |

The current implementation provides initial privacy safeguards but does not establish complete privacy protection or regulatory compliance.

## 8. Accessibility Event Observation (RQ1)

### 8.1 Objective and Procedure

The Month 2 regression tests verified consent and monitoring control but did not show which interaction data Android actually delivers. A dedicated observation test was therefore performed to answer the first part of RQ1: which interaction metadata the AccessibilityService receives on a real device, and in what form.

| Item | Value |
|---|---|
| Date | November 25, 2026 |
| Device | Samsung Galaxy S21 Ultra (SM-G998N), Android 15 |
| Keyboard | Samsung Keyboard (device default) |
| Service configuration | `canRetrieveWindowContent="false"`, three event types, `notificationTimeout="0"` |
| Monitoring state | Consent given, monitoring enabled |

For this test only, temporary diagnostic logging was added to the service. It wrote to Logcat on the development computer: the `addedCount` and `removedCount` of text-change events; the `scrollDeltaY`, `scrollY`, `maxScrollY`, `fromIndex`, `toIndex`, and `itemCount` of scroll events; and the package name of window-state changes, to check the exclusion filter. Event text was never read or logged. The package-name logging is a development diagnostic and is removed before any participant use.

In line with the privacy design, third-party applications are described generically in this report.

The test actions were: typing a short word and deleting characters in a note-taking application and a messaging application; accepting an autocorrect suggestion; typing in the password fields of two login screens; scrolling in several applications; and switching between applications.

### 8.2 Text-Change Events

Two distinct reporting patterns were observed.

| Pattern | Where observed | Typing "hello" | One backspace |
|---|---|---|---|
| A. Single-character | Google sign-in screen; third-party login screen | Five events of `added=1 removed=0` | `added=0 removed=1` |
| B. Word replacement (composing text) | Samsung Messages; Samsung Notes | `1/0`, `2/1`, `3/2`, `4/3`, `5/4` (added/removed) | `added=4 removed=5` |

In pattern B, the keyboard replaces the whole word being composed on every keystroke, so each event reports the word's new and previous length. Samsung Notes additionally produced an event with equal counts (e.g., `added=3 removed=3`) after most keystrokes, which does not correspond to a keystroke and is most likely an update of the composing-text underline. Deleting existing text outside the word being composed produced `added=0 removed=1` in these applications as well (observed in Samsung Notes). One event reported an invalid value (`removed=-1`).

**Consequence.** Classifying events by "`added=1`, `removed=0` is a typed character" would detect only the first character of each word in pattern B and no deletions at all. Events are therefore classified by their **net change**, `addedCount − removedCount`:

| Net change | Classification |
|---|---|
| +1 | Typed character |
| −1 | Deletion |
| 0 | Not a keystroke; ignored |
| > +1 or < −1 | Replacement (autocorrect, suggestion, paste, or field clearing) |
| Invalid value | Skipped and counted as invalid (NFR04) |

Applied to the messaging-application test, this rule yields five typed characters followed by two deletions, which matches the actions performed. The rule correctly handles both patterns.

### 8.3 Scroll Events

Three levels of detail were observed, depending on the application:

| Detail available | Observed in | Example values |
|---|---|---|
| None | Launcher and most applications | `dY=0`, `fromIndex=-1`, `toIndex=-1` |
| List positions only | Messaging application lists | `from=0 to=3 items=29` |
| Pixel distances | Play Store | `dY` between −2402 and 2055, with `scrollY` and `maxScrollY` |

During continuous scrolling, events arrived approximately every 100–115 ms, regardless of the gesture. The raw number of scroll events therefore reflects how long the user scrolls, not how many separate scroll gestures were made.

**Consequence.** Scroll distance cannot be the primary scroll feature, because most applications do not report it. Scroll events less than about 300 ms apart are grouped into one **scroll gesture**; the number and duration of gestures are the primary scroll features, and distance is recorded only where an application reports it. Launcher page swipes also produced scroll events and are excluded.

### 8.4 Window-State Changes and Application Switching

Every change of foreground application produced a window-state event, but the raw events overestimate application switching:

- **Launcher.** Moving from one application to another via the home screen produced two switches (application → launcher → application), because the launcher is reported like an application.
- **Overlay flashes.** A preinstalled payment-service overlay repeatedly appeared between the launcher and other windows for 9–45 ms (e.g., launcher → overlay → launcher within 45 ms).
- **Rapid flips.** When an application was opened from the app store, three window changes between the two applications occurred within 5 ms.
- **System dialogs.** Android's credential manager appeared as a separate window during login.

**Consequence.** An application switch is counted only when a different application stays in the foreground for at least about one second, and the launcher and system overlays are not counted as applications.

### 8.5 Self-Exclusion and Monitoring State

No events were logged while the Burnout Detector application itself was in the foreground, confirming the self-exclusion rule. Events performed while monitoring was paused, including after each application restart from Android Studio, were not processed, confirming the monitoring gate.

### 8.6 Password-Field Test

Characters were typed into the password fields of two login screens: the Google account sign-in screen provided by Google Play services, and the custom login screen of a third-party social media application.

| Login screen | Result |
|---|---|
| Google account sign-in | Text-change events received (`added=1 removed=0`), fewer events than characters typed |
| Third-party application login | Text-change events received, one per character typed (`added=1 removed=0`) |

On both screens, the events passed the `isPassword()` check, which means that these fields were **not marked as password fields** for accessibility, most likely because the screens use web-based or custom input fields. The application never received the typed text, because event text is never read. However, per-event character counts and timestamps would reveal the **length and typing rhythm** of a password.

**Consequence.** Password detection cannot be relied on as the only protection. The design was changed accordingly:

1. **No per-event storage.** Events are counted in memory, and only five-minute window totals are stored, so the length of a single input cannot be recovered. The temporary `InteractionEvent` table was removed from the database design.
2. **Login-context exclusions.** Text-change events from system sign-in and credential components (Google Play services and Android's credential manager) are ignored.

The updated privacy and database designs are described in `privacy-design.md` (Sections 5.2 and 8) and `database-design.md` (Sections 3.1 and 6). This result is the concrete example of the best-effort limitation stated in the participant information.

### 8.7 Summary of Findings and Design Decisions

| Finding | Design decision for Month 3 |
|---|---|
| Two text-change reporting patterns (single character and word replacement) | Classify by net change, `addedCount − removedCount` |
| Equal-count and invalid text-change events | Ignore equal-count events; skip and count invalid events |
| Scroll distance mostly unavailable; events every ~100 ms | Group events into gestures; distance only where reported |
| Launcher, overlays and rapid flips inflate application switches | One-second minimum dwell time; exclude launcher and overlays |
| Password fields not reliably flagged | No per-event storage; exclude login and credential components |

These findings answer the first part of RQ1 for this device: typing, deletion, scrolling and application switching can be observed without reading text, but only after device-specific processing. The second part of RQ1, consistency across manufacturers and keyboards, requires repeating this test on a second device.

## 9. Testing Limitations and Outstanding Risks

### 9.1 Concurrent Consent Withdrawal

Withdrawal during a pending activation operation remains inconclusive.

A controlled integration test should deliberately delay the monitoring preference update and initiate consent withdrawal while that operation is pending.

The test should verify that monitoring remains blocked and that persistent consent is removed successfully.

### 9.2 DataStore Failure Recovery

Preference read and write error handling has been implemented.

However, actual storage corruption, read failures, and write failures have not been deliberately induced.

Controlled failure-injection testing is required to verify these recovery mechanisms.

### 9.3 Activity Destruction During Withdrawal

The current user interface uses lifecycle
