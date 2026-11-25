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

They do not demonstrate behavioral metric extraction or database persistence because those components have not yet been implemented.

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
| Password-flagged event rejection | Verified in filtering logic |
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

## 8. Testing Limitations and Outstanding Risks

### 8.1 Concurrent Consent Withdrawal

Withdrawal during a pending activation operation remains inconclusive.

A controlled integration test should deliberately delay the monitoring preference update and initiate consent withdrawal while that operation is pending.

The test should verify that monitoring remains blocked and that persistent consent is removed successfully.

### 8.2 DataStore Failure Recovery

Preference read and write error handling has been implemented.

However, actual storage corruption, read failures, and write failures have not been deliberately induced.

Controlled failure-injection testing is required to verify these recovery mechanisms.

### 8.3 Activity Destruction During Withdrawal

The current user interface uses lifecycle
