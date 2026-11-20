# Research Foundation
## Problem Statement
Mental fatigue is a state that can arise following prolonged cognitive activity and may influence attention, performance, and behavior. As smartphones are widely used for communication, studying, and everyday tasks, changes in mobile interaction patterns may provide useful information about users' cognitive states.

Conventional approaches to assessing mental fatigue often involve self-report questionnaires, experimental tasks, or physiological measurements. While these approaches can provide valuable information, they may require active participation or additional equipment, which can limit their suitability for continuous monitoring.

This thesis investigates the feasibility of developing an Android application that passively collects privacy-conscious interaction metadata and analyzes behavioral changes potentially associated with mental fatigue. The proposed application focuses on typing-related activity, correction behavior, scrolling patterns, and application switching frequency.

The system will aggregate interaction features over five-minute time windows and compare them with a personalized behavioral baseline. Deviations from typical interaction patterns will subsequently be evaluated using a lightweight, rule-based anomaly detection approach.

Privacy is a central requirement of the system. The application will be designed to avoid recording the content of typed text and to store collected information locally on the user's device.

The research aims to investigate whether measurable changes in smartphone interaction behavior can be associated with self-reported mental fatigue and whether such monitoring can be implemented reliably using Android system services.

The proposed application is intended as an exploratory fatigue-awareness tool rather than a clinical diagnostic system.
##Research Questions
RQ1: To what extent can Android AccessibilityService provide measurable interaction metadata for behavioral analysis without recording the content of typed text?

RQ2: Which smartphone interaction features, such as typing-related activity, correction behavior, scrolling variability, and application switching frequency, show associations with self-reported mental fatigue?

RQ3: Can a lightweight, personalized, rule-based anomaly detection approach identify behavioral deviations associated with elevated self-reported mental fatigue?
## Research Hypotheses
H1: Changes in smartphone interaction features relative to an individual's personalized baseline are associated with changes in self-reported mental fatigue.

H0: Changes in smartphone interaction features relative to an individual's personalized baseline are not associated with changes in self-reported mental fatigue.

##Research Objectives
1.Review existing scientific literature on mental fatigue, keystroke dynamics, and mobile interaction analysis.

2.Investigate the technical feasibility of collecting interaction metadata through Android AccessibilityService.

3.Design and implement a privacy-conscious Android application using Kotlin and Room.

4.Extract measurable interaction features using five-minute time windows.

5.Develop a personalized baseline and rule-based anomaly detection approach.

6.Evaluate the system using device testing and participants' self-reported fatigue measurements.
## Scope and Limitations
The application will target Android devices and operate using locally stored behavioral data. The system will not capture the textual content entered by users.

The availability and accuracy of interaction features may vary depending on the Android version, device manufacturer, keyboard application, and accessibility event behavior.

The research will focus on possible indicators of short-term mental fatigue. It will not attempt to diagnose clinical burnout or other medical conditions.

A small participant study will be used for exploratory evaluation, subject to the necessary consent and institutional approvals.
