\# Problem Statement



Mental fatigue is a state that can arise following prolonged cognitive activity and may influence attention, performance, and behavior. As smartphones are widely used for communication, studying, and everyday tasks, changes in mobile interaction patterns may provide useful information about users' cognitive states.



Conventional approaches to assessing mental fatigue often involve self-report questionnaires, experimental tasks, or physiological measurements. While these approaches can provide valuable information, they may require active participation or additional equipment, which can limit their suitability for continuous monitoring.



This thesis investigates the feasibility of developing an Android application that passively collects privacy-conscious interaction metadata and analyzes behavioral changes potentially associated with mental fatigue. The proposed application focuses on typing-related activity, correction behavior, scrolling patterns, and application switching frequency.



The system will aggregate interaction features over five-minute time windows and compare them with a personalized behavioral baseline. Deviations from typical interaction patterns will subsequently be evaluated using a lightweight, rule-based anomaly detection approach.



Privacy is a central requirement of the system. The application will be designed to avoid recording the content of typed text and to store collected information locally on the user's device.



The research aims to investigate whether measurable changes in smartphone interaction behavior can be associated with self-reported mental fatigue and whether such monitoring can be implemented reliably using Android system services.



The proposed application is intended as an exploratory fatigue-awareness tool rather than a clinical diagnostic system.



\## Scope and Limitations



The application will target Android devices and operate using locally stored behavioral data. The system will not capture the textual content entered by users.



The availability and accuracy of interaction features may vary depending on the Android version, device manufacturer, keyboard application, and accessibility event behavior.



The research will focus on possible indicators of short-term mental fatigue. It will not attempt to diagnose clinical burnout or other medical conditions.



A small participant study will be used for exploratory evaluation, subject to the necessary consent and institutional approvals.

