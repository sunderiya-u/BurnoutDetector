\# Month 2 — Completion Summary



\*\*Project:\*\* Burnout Detector: Passive Mobile-Based Mental Fatigue Detection Using Typing and Interaction Patterns



\*\*Completion date:\*\* November 25, 2026



\*\*Status:\*\* Prototype implementation and planned verification completed, subject to documented testing limitations.



\## 1. Month 2 Objectives



The primary objective of Month 2 was to establish the Android application foundation and design a privacy-conscious architecture before implementing behavioral data collection.



The development activities focused on system architecture, database design, privacy protection, consent management, accessibility event observation, and monitoring control.



\## 2. Completed Deliverables



| Deliverable | Status |

|---|---|

| System architecture design | Completed |

| Database schema design | Completed |

| Privacy architecture design | Completed |

| Android project initialization | Completed |

| Research consent interface | Completed |

| Persistent consent management | Completed |

| Monitoring dashboard | Completed |

| AccessibilityService prototype | Completed |

| Privacy event filtering | Completed |

| Immediate monitoring gate | Completed |

| Safe process restart mechanism | Completed |

| Android backup protection | Completed |

| Consent withdrawal priority | Implemented; concurrent scenario requires further testing |

| Preference error handling | Implemented; failure-injection testing pending |

| Automated unit testing | Completed |

| Physical device regression testing | Completed with one inconclusive result |

| Implementation documentation | Completed |

| Testing documentation | Completed |



\## 3. Technical Achievements



During Month 2, the following technical components were implemented:



\- Kotlin-based Android application using Jetpack Compose.

\- Explicit research consent and persistent preference management using Jetpack DataStore.

\- AccessibilityService prototype supporting text-change, scrolling, and window-state-change events.

\- Independent privacy event filtering.

\- In-memory monitoring permission control.

\- Generation-based invalidation of outdated activation requests.

\- Safe monitoring behavior after application process restart.

\- Android backup and device-transfer exclusion configurations.

\- Consent withdrawal prioritization.

\- Preference read and write error handling.



The implementation establishes a foundation for future behavioral feature extraction while avoiding access to actual typed content in the implemented event-processing logic.



\## 4. Verification Results



\### 4.1 Automated Unit Tests



| Test class | Passed | Failed |

|---|---:|---:|

| ExampleUnitTest | 1 | 0 |

| PrivacyEventFilterTest | 13 | 0 |

| MonitoringGateTest | 6 | 0 |

| Total | 20 | 0 |



All 20 unit tests passed, with zero skipped tests.



The reported test run duration was 0.927 seconds.



\### 4.2 Physical Device Regression Tests



Testing was performed on a Samsung Galaxy S21 Ultra running Android 15.



| Result | Number of tests |

|---|---:|

| Passed | 9 |

| Failed | 0 |

| Inconclusive | 1 |

| Total | 10 |



The inconclusive scenario concerns consent withdrawal during a pending monitoring activation operation.



\### 4.3 Additional Verification



Additional verification activities included:



\- Accessibility service connection verification.

\- Monitoring permission observation through Logcat.

\- Application process restart testing.

\- Physical device reboot testing.

\- Merged manifest inspection.

\- Installed application backup flag inspection.

\- Android Backup Manager rejection testing.



The Android Backup Manager explicitly rejected the backup request for the application.



\## 5. Privacy Safeguards



The current prototype incorporates the following privacy safeguards:



\- Explicit consent before monitoring activation.

\- Monitoring disabled by default.

\- Immediate monitoring permission revocation.

\- Manual monitoring reactivation after process restart.

\- Password-flagged event rejection.

\- Selected system and keyboard application exclusions.

\- Restricted accessibility event categories.

\- No access to actual typed text in the implemented service.

\- No screenshot collection.

\- No persistent application package-name storage.

\- No declared Internet permission.

\- Android backup restrictions.

\- Minimal persistent preference storage.



The current prototype stores consent and monitoring preferences but does not extract or persist behavioral metrics.



\## 6. Outstanding Limitations



The following limitations remain:



1\. Consent withdrawal during pending activation requires controlled integration testing.

2\. DataStore read and write failure recovery requires failure-injection testing.

3\. Activity destruction during consent withdrawal requires additional lifecycle verification.

4\. Physical device-to-device migration has not been tested.

5\. Sensitive application exclusion and password-event identification are not exhaustive.

6\. Accessibility event delivery may not accurately represent individual physical keystrokes.

7\. Physical device testing has primarily been conducted on one Android device.

8\. Behavioral data storage, export, and deletion mechanisms have not yet been implemented.



These limitations must be addressed as appropriate before actual behavioral data collection and research participant deployment.



\## 7. Transition to Month 3



Month 3 will focus on implementing behavioral feature extraction and local data storage.



\### 7.1 Planned Development Activities



\- Implement Room database entities and data access objects.

\- Implement a local data repository.

\- Extract sanitized typing-related metadata.

\- Extract scrolling-related metadata.

\- Implement application-switch counting without persistent package-name storage.

\- Implement five-minute behavioral feature aggregation.

\- Introduce monitoring-session-aware data processing.

\- Implement safe cancellation of pending metric-processing operations.

\- Implement local behavioral data deletion and export mechanisms.

\- Conduct controlled feature extraction experiments.

\- Expand automated and physical device testing.



\### 7.2 Expected Outcome



The expected outcome of Month 3 is a functional local behavioral data collection prototype capable of extracting and aggregating selected smartphone interaction metrics while respecting the established privacy and consent architecture.



Behavioral metrics must be evaluated for reliability before they are used for personalized baseline calculation or fatigue detection.



\## 8. Conclusion



Month 2 established the technical foundation of the Burnout Detector research application.



The consent interface, preference management, monitoring controls, accessibility event observation, privacy filtering, and backup restrictions have been implemented and evaluated at the prototype level.



All 20 automated unit tests passed. Nine of ten final physical device regression tests passed, with one inconclusive scenario and no observed failures.



The project is ready to proceed to controlled Month 3 development, subject to the documented limitations and further privacy verification.



The current prototype is not a medical diagnostic system and is not yet ready for research participant deployment.
