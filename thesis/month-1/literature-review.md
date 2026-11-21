# Chapter 2. Literature Review and Related Work

## 2.1. Mental Fatigue and Digital Behavioral Indicators

Mental fatigue is a complex state associated with sustained cognitive demands and changes in alertness, attention, and task performance. Detecting mental fatigue in everyday environments is challenging because traditional assessment methods often require users to actively complete questionnaires or cognitive tests. These approaches can provide valuable information but may interrupt normal activities and are not always suitable for continuous monitoring.

The widespread use of smartphones has created opportunities to investigate behavioral indicators of cognitive functioning through everyday digital interactions. Smartphone usage generates measurable patterns, including typing activity, scrolling, and application switching. Such patterns may provide information about changes in user behavior without requiring continuous active assessment.

Fang et al. (2026) investigated the potential of smartphone typing metrics for unobtrusive mental fatigue assessment. Their study demonstrated an association between typing speed and time awake, suggesting that everyday smartphone interactions may contain information relevant to changes in alertness. However, time awake represents an indirect indicator of fatigue and should not be interpreted as a direct measurement of an individual's subjective mental fatigue.

Consequently, behavioral monitoring systems require careful validation against independent measures of fatigue. Changes in smartphone interaction behavior should be treated as potential indicators rather than definitive evidence of a fatigued state.

## 2.2. Keystroke Dynamics and Cognitive Functioning

Keystroke dynamics refers to measurable characteristics of typing behavior, such as typing speed, timing between keypresses, and correction-related activity. These features have been investigated as potential indicators of cognitive functioning because typing involves coordinated motor activity, attention, and information processing.

Ross et al. (2021) investigated whether naturalistic smartphone typing dynamics were associated with cognitive performance. The researchers used a custom Android keyboard to collect typing-related information from 19 participants, including individuals with bipolar disorder and nonbipolar controls. Typing measurements were compared with performance on digital Trail Making Tests and mood assessments.

The researchers found that slower typing was associated with longer completion times on the digital Trail Making Test Part B. This result suggests that smartphone typing dynamics may reflect aspects of processing speed and executive functioning.

The study is relevant to the present thesis because it demonstrates the potential value of typing-related measurements as behavioral features. In particular, typing speed may provide information about changes in cognitive performance during everyday smartphone use.

Nevertheless, several limitations must be considered. The study involved a relatively small sample and examined cognitive performance rather than mental fatigue directly. Additionally, the researchers relied on a custom Android keyboard, which provided access to detailed keypress information. Such measurements may not be available through Android AccessibilityService alone.

Therefore, the findings support the investigation of typing-related features but do not establish their effectiveness for detecting mental fatigue using standard Android accessibility events.

## 2.3. Smartphone Typing Patterns and Mental Fatigue

Fang et al. (2026) examined the relationship between smartphone typing performance and time awake among first-year physicians in the United States. The researchers analyzed 45,042 typing sessions collected from 366 participants over approximately two months.

Typing measurements were obtained through Apple's SensorKit framework, while wearable devices provided sleep-related information. The researchers investigated changes in typing speed and deletion rate in relation to the duration of wakefulness.

Their analysis identified a nonlinear relationship between typing speed and time awake. At the population level, typing speed increased during the earlier waking period, reached a peak approximately 7.5 hours after waking, and subsequently declined after prolonged wakefulness, with a notable reduction around 15.3 hours awake.

These findings suggest that smartphone typing performance may be influenced by factors associated with alertness and prolonged wakefulness. Importantly, the relationship was not simply linear, indicating that behavioral changes may depend on the user's current context and time since waking.

The study provides a strong motivation for investigating typing speed as a candidate feature in mental fatigue monitoring. It also supports considering individual differences when interpreting interaction patterns.

However, the study has several limitations relevant to the present research. First, time awake is an indirect proxy for mental fatigue rather than an independent subjective fatigue measurement. Second, the participant population consisted of physicians undergoing demanding professional training, which may limit generalizability to other groups. Third, data were collected using Apple's SensorKit rather than Android AccessibilityService.

These differences highlight the need to investigate whether similar typing-related features can be collected reliably on Android devices and whether their variations are associated with self-reported mental fatigue.

## 2.4. Passive Smartphone Sensing and Behavioral Monitoring

Passive smartphone sensing involves collecting behavioral or contextual measurements through mobile devices with limited active participation from users. This approach has been explored in research investigating mental well-being, daily routines, and behavioral changes.

Wang et al. (2014) introduced StudentLife, an Android-based smartphone sensing system designed to investigate mental health, academic performance, and behavioral trends among university students. The researchers monitored 48 Dartmouth College students over a ten-week academic term.

The system collected information related to activity, sleep, mobility, and social behavior. Passive sensing measurements were combined with self-report questionnaires and ecological momentary assessments to investigate relationships between behavioral patterns and participants' experiences.

The study identified significant associations between automatically sensed behavioral measurements and several mental-health and educational outcomes. It also reported changes in stress, sleep, activity, and positive affect across the academic term as students experienced increasing academic demands.

These findings demonstrate the potential of smartphone-based sensing for studying everyday behavior in naturalistic environments. The StudentLife methodology is particularly relevant because it combines passive measurements with participant-reported information.

However, StudentLife investigated broad mental-health and behavioral outcomes rather than short-term mental fatigue inferred from typing, scrolling, and application switching. Its results therefore cannot directly establish the accuracy of the proposed Burnout Detector system.

For the present thesis, StudentLife provides a methodological reference for combining passive smartphone measurements with self-reported fatigue ratings during a participant study.

## 2.5. Critical Comparison of Existing Research

The three reviewed studies demonstrate different but complementary approaches to investigating relationships between smartphone behavior and psychological or cognitive outcomes.

Ross et al. (2021) focused on the relationship between typing dynamics and cognitive performance. Their study provides evidence that naturalistic typing measurements may be associated with processing speed and executive functioning. However, the relatively small sample and reliance on a custom keyboard limit the direct applicability of the findings to a general-purpose Android monitoring application.

Fang et al. (2026) investigated typing behavior in relation to prolonged wakefulness using a substantially larger dataset. Their findings provide more direct motivation for studying fatigue-related changes in typing speed. Nevertheless, their use of time awake as a proxy for fatigue and reliance on Apple's SensorKit introduce important methodological and technical differences from the proposed system.

Wang et al. (2014) examined a broader range of smartphone-sensed behaviors and demonstrated the value of combining passive monitoring with self-report assessments. Although the study did not focus on typing-based fatigue detection, it provides useful guidance for designing longitudinal behavioral studies.

Across these studies, three important considerations emerge.

First, smartphone interaction measurements may provide useful information about cognitive and psychological states, but associations should not be interpreted as proof of causation or diagnostic accuracy.

Second, the technical capabilities of the data collection method strongly influence which features can be measured. Custom keyboards, operating-system sensing frameworks, and Android accessibility services do not necessarily provide equivalent information.

Third, validation against independently collected participant assessments is necessary before interpreting behavioral deviations as indicators of mental fatigue.

These considerations inform the design and evaluation strategy of the present thesis.

## 2.6. Research Gap

The reviewed literature provides evidence that smartphone typing dynamics can be associated with cognitive performance and prolonged wakefulness, while broader passive smartphone sensing can reveal relationships between everyday behavior and mental well-being.

However, the three studies reviewed do not directly establish whether a lightweight Android application using AccessibilityService can reliably collect privacy-conscious interaction features and use personalized behavioral baselines to identify deviations associated with self-reported mental fatigue.

In particular, several questions remain relevant to the proposed implementation:

1. Which typing-related, scrolling, and application-switching features can be reliably extracted from Android accessibility events without retaining textual content?
2. How can these features be aggregated into short time windows while preserving user privacy?
3. Can deviations from an individual's typical interaction patterns be meaningfully associated with self-reported mental fatigue?
4. How consistent is feature availability across different Android devices and software configurations?

The present thesis addresses these questions through the design and implementation of a native Android application using Kotlin, AccessibilityService, and Room for local data storage.

The proposed system will extract feasible interaction features, aggregate them into five-minute time windows, and subsequently compare the resulting measurements with personalized behavioral baselines. A lightweight rule-based anomaly detection approach will be investigated, with its outputs evaluated against participant-reported fatigue levels.

The intended contribution is an exploratory, privacy-conscious Android implementation and an evaluation of its technical feasibility and potential usefulness for mental fatigue monitoring. The application is not intended to diagnose clinical burnout.

## References

Fang, Y., Yang, P., Frank, E., Goldstein, C., Wright, A. G. C., Bohnert, A. S. B., Kheterpal, V., Sen, S., & Wu, Z. (2026). Patterns of smartphone typing performance by time awake: Implications for unobtrusive ambulatory mental fatigue assessment. *PLOS Digital Health, 5*(3), e0001281. https://doi.org/10.1371/journal.pdig.0001281

Ross, M. K., Demos, A. P., Zulueta, J., Piscitello, A., Langenecker, S. A., McInnis, M., Ajilore, O., Nelson, P. C., Ryan, K. A., & Leow, A. (2021). Naturalistic smartphone keyboard typing reflects processing speed and executive function. *Brain and Behavior, 11*(11), e2363. https://doi.org/10.1002/brb3.2363

Wang, R., Chen, F., Chen, Z., Li, T., Harari, G. M., Tignor, S., Zhou, X., Ben-Zeev, D., & Campbell, A. T. (2014). StudentLife: Assessing mental health, academic performance and behavioral trends of college students using smartphones. In *Proceedings of the 2014 ACM International Joint Conference on Pervasive and Ubiquitous Computing* (pp. 3–14). ACM. https://doi.org/10.1145/2632048.2632054
