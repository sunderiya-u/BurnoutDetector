# Chapter 2. Literature Review and Related Work

This chapter reviews the research on which the Burnout Detector builds. Section 2.1 clarifies what mental fatigue is and how it differs from sleepiness and burnout, and Section 2.2 explains why passive measurement is attractive. Sections 2.3 to 2.6 review four groups of empirical studies: keystroke dynamics on physical keyboards, smartphone typing, smartphone touch interaction and usage, and broader passive smartphone sensing. Section 2.7 examines Android AccessibilityService, the data source used in this thesis, including its documented privacy risks. Section 2.8 compares the studies, and Section 2.9 derives the research gap that the research questions address.

## 2.1. Mental Fatigue, Sleepiness, and Burnout

Mental fatigue is a complex state associated with sustained cognitive demands and changes in alertness, attention, and task performance. Boksem and Tops (2008) propose that it arises when the perceived energetic costs of continuing a task come to outweigh the rewards that a person expects from it. In this account, fatigue acts as a signal that reduces the motivation to keep investing effort in the current task. A fatigued person may still be able to perform, but becomes less willing to do so carefully and persistently. This view is relevant to the present thesis because it suggests that fatigue may be visible in ordinary behavior, such as typing more slowly or making and correcting more errors, and not only in laboratory tests.

Mental fatigue must be distinguished from two related concepts.

**Burnout.** The World Health Organization includes burn-out in the ICD-11 as an occupational phenomenon that results from chronic workplace stress that has not been successfully managed. It is characterized by three dimensions: exhaustion, increased mental distance from one's job, and reduced professional efficacy. It is not classified as a medical condition (World Health Organization, 2019). Because burnout results from chronic stress, it typically develops over an extended period and refers specifically to the work context, so it cannot be established from a few days of smartphone use. Repeated short-term fatigue without sufficient recovery may contribute to it, but the present thesis makes no claim to detect burnout. The name of the application refers to its motivation, not to its measurement target.

**Sleepiness.** Sleepiness refers to the tendency to fall asleep and depends strongly on time awake and time of day. It overlaps with mental fatigue, because both are associated with reduced alertness and performance, and the two often occur together. They are nevertheless conceptually distinct: a person may feel mentally exhausted after hours of demanding work without being sleepy, or sleepy after a short night without having done demanding work.

This distinction matters for measurement. The present thesis uses the Karolinska Sleepiness Scale (KSS) as its in-the-moment self-report (see [`research-questions.md`](research-questions.md)). The KSS is a single-item, nine-point scale ranging from 1 (extremely alert) to 9 (very sleepy, fighting sleep). It was introduced by Åkerstedt and Gillberg (1990) in a study comparing subjective and objective sleepiness in active individuals. For this project, its main advantages are that it takes only a few seconds to answer, is widely used, and refers to the present moment, which allows each rating to be linked to the five-minute interaction windows recorded shortly before it. A rating of 7 corresponds to feeling sleepy but having no difficulty remaining awake, which is why KSS ≥ 7 is used as the threshold for elevated fatigue.

The limitation of this choice is that the KSS measures sleepiness rather than mental fatigue in the narrower sense of Boksem and Tops (2008). Results obtained with it will therefore be interpreted as relating to reduced alertness, an important component of short-term fatigue, and the choice of ground truth is discussed as a limitation of the study.

## 2.2. From Active Assessment to Passive Behavioral Indicators

Detecting mental fatigue in everyday environments is challenging because traditional assessment methods often require users to actively complete questionnaires or cognitive tests. The Psychomotor Vigilance Task (PVT), a common objective measure of alertness, requires several minutes of sustained attention each time it is taken (Fang et al., 2026). These approaches can provide valuable information but interrupt normal activities and are not well suited to continuous monitoring.

The effort required by active measurement also affects how long people keep using a tool. Research on personal informatics shows that people commonly lapse in or abandon self-tracking tools over time (Epstein et al., 2016). A fatigue-awareness tool that depends on regular manual input is therefore likely to be used only for a short period.

The widespread use of smartphones has created opportunities to investigate behavioral indicators of cognitive functioning through everyday digital interactions. Smartphone usage generates measurable patterns, including typing activity, scrolling, and application switching. Such patterns may provide information about changes in user behavior without requiring continuous active assessment. Several studies, reviewed in Sections 2.3 to 2.6, have related such patterns to stress, emotion, cognitive performance, and alertness.

However, behavioral monitoring systems require careful validation against independent measures of fatigue. Changes in smartphone interaction behavior should be treated as potential indicators rather than definitive evidence of a fatigued state. Passive measurement therefore does not remove self-report entirely: during evaluation, a ground truth is still needed. The present thesis uses a small number of brief KSS prompts per day for this purpose, while the behavioral features themselves are collected without any effort from the user.

## 2.3. Keystroke Dynamics on Physical Keyboards

Keystroke dynamics refers to measurable characteristics of typing behavior, such as typing speed, the timing between keypresses, and correction-related activity. These features have been investigated as potential indicators of cognitive and emotional states because typing involves coordinated motor activity, attention, and information processing. The earliest studies used physical computer keyboards, which record the exact moment at which each key is pressed and released.

Vizer, Zhou and Sears (2009) explored whether cognitive and physical stress can be detected from keyboard interaction. Twenty-four participants wrote texts under a cognitive stress condition, a physical stress condition, and a neutral condition. The researchers extracted keystroke features, such as the number of backspace presses, together with linguistic features of the spontaneously written text, and used machine learning to classify the conditions. They reported that the stress conditions could be distinguished from the non-stress condition with accuracies comparable to other affective computing methods: 75% for cognitive stress and 62.5% for physical stress relative to the neutral condition.

The study is relevant because it was among the first to propose keyboard monitoring as an unobtrusive way to detect changes in cognitive function, and because correction behavior (backspace use) was one of its features, a direct precursor of the deletion-based correction ratio used in this thesis. Its limitations are that stress was induced in a laboratory and that linguistic features require access to the written text, which the present thesis deliberately avoids.

Epp, Lippold and Mandryk (2011) moved keystroke analysis into the field. Participants' keystrokes were recorded on standard keyboards in situ, and participants periodically reported their emotional state through experience sampling. From this data, the researchers built classifiers for 15 emotional states. Two levels of six states, namely confidence, hesitance, nervousness, relaxation, sadness, and tiredness, could be classified with accuracies of approximately 77% to 88%.

This study is relevant for two reasons. First, tiredness was among the states that could be recognized from typing behavior. Second, its design, in which passively recorded behavior is compared with short self-reports collected at intervals, is the same basic design used in the present thesis. However, its features were based on precise key-press durations and the latencies between keys, and its best models used timing features from a fixed passage that participants typed at each prompt.

Together, these studies show that typing behavior can reflect internal states and that experience sampling is a practical way to obtain ground truth. Both, however, depend on detailed key-press and key-release timings from physical keyboards. As Section 2.7 explains, this level of detail is not available to an Android application that observes typing through accessibility events.

## 2.4. Smartphone Typing and Cognitive State

Typing on a smartphone differs from typing on a physical keyboard. It is usually done with one or two thumbs on a touchscreen keyboard, it is frequently assisted by autocorrection and word suggestions, and it happens in short bursts throughout the day. Three studies have examined whether smartphone typing nevertheless reflects cognitive state.

### 2.4.1. Typing Dynamics and Executive Function

Ross et al. (2021) investigated whether naturalistic smartphone typing dynamics were associated with cognitive performance. The researchers used a custom Android keyboard to collect typing-related information from 19 participants, including individuals with bipolar disorder and nonbipolar controls. Typing measurements were compared with performance on digital Trail Making Tests and mood assessments.

The researchers found that slower typing was associated with longer completion times on the digital Trail Making Test Part B. This result suggests that smartphone typing dynamics may reflect aspects of processing speed and executive functioning.

The study is relevant to the present thesis because it demonstrates the potential value of typing-related measurements as behavioral features. In particular, typing speed may provide information about changes in cognitive performance during everyday smartphone use.

Nevertheless, several limitations must be considered. The study involved a relatively small sample and examined cognitive performance rather than mental fatigue directly. Additionally, the researchers relied on a custom Android keyboard, which provided access to detailed keypress information. Such measurements may not be available through Android AccessibilityService alone, and a custom keyboard requires participants to replace the keyboard they normally use.

Therefore, the findings support the investigation of typing-related features but do not establish their effectiveness for detecting mental fatigue using standard Android accessibility events.

### 2.4.2. Typing Performance and Time Awake

Fang et al. (2026) examined the relationship between smartphone typing performance and time awake among first-year physicians in the United States. The researchers analyzed 45,042 typing sessions collected from 366 participants over approximately two months.

Typing measurements were obtained through Apple's SensorKit framework, while wearable devices provided sleep-related information. The researchers investigated changes in typing speed and deletion rate in relation to the duration of wakefulness.

Their analysis identified a nonlinear relationship between typing speed and time awake. At the population level, typing speed increased during the earlier waking period, reached a peak approximately 7.5 hours after waking, and subsequently declined after prolonged wakefulness, with a notable reduction around 15.3 hours awake. Deletion rate was also significantly associated with time awake, but the change was minimal, and the authors concluded that it was a less sensitive indicator of mental fatigue than typing speed.

These findings suggest that smartphone typing performance may be influenced by factors associated with alertness and prolonged wakefulness. Importantly, the relationship was not simply linear, indicating that behavioral changes may depend on the user's current context and time since waking. For the present thesis, this means that a slower typing speed in the late evening may partly reflect a normal daily rhythm. The timestamp of each interaction window should therefore be retained and considered when behavior is compared with a personal baseline.

The study provides a strong motivation for investigating typing speed as a candidate feature in mental fatigue monitoring. It also supports considering individual differences when interpreting interaction patterns.

However, the study has several limitations relevant to the present research. First, time awake is an indirect proxy for mental fatigue rather than an independent subjective fatigue measurement. Second, the participant population consisted of physicians undergoing demanding professional training, which may limit generalizability to other groups. Third, data were collected using Apple's SensorKit rather than Android AccessibilityService.

These differences highlight the need to investigate whether similar typing-related features can be collected reliably on Android devices and whether their variations are associated with self-reported mental fatigue.

### 2.4.3. Typing Metrics and Vigilance

Visuri et al. (2025) conducted a two-month field study that combined the Psychomotor Vigilance Task, smartphone typing metrics, and sleep data from an Oura ring. They found significant correlations between PVT performance and both typing speed and typing error rates.

This study complements Fang et al. (2026) in an important way. Instead of relying on time awake as a proxy, it compared typing behavior with an objective measure of alertness. Its results support both typing speed and error-related behavior as candidate indicators, which corresponds to hypotheses H1a and H1b of the present thesis.

Its limitations for the present work are that the PVT itself requires active participation, so the study demonstrates an association rather than a fully passive system, and that it does not examine Android AccessibilityService as a data source. Furthermore, an application that never reads typed text cannot know which characters were typed incorrectly. In the present thesis, error behavior can only be approximated through deletions, which is why the correction ratio is defined on the basis of deletions rather than errors.

## 2.5. Touch Interaction and Smartphone Usage Patterns

Typing is only one form of smartphone interaction. Ciman and Wac (2018) investigated whether stress can be assessed from human–smartphone interaction in a way that avoids both invasive external devices and privacy-sensitive information. They presented two approaches. The first analyzed touch gestures, namely tapping, scrolling, swiping, and text input, and was evaluated in laboratory settings with 13 participants. It reached an F-measure of 79% to 85% with within-subject models and 70% to 80% with a global model. The second analyzed how the smartphone was used and was evaluated in the wild with 25 participants, reaching an F-measure of 77% to 88% with within-subject models and 63% to 83% with a global model.

This study is relevant in three ways. First, it is the only reviewed study that uses scrolling and swiping as features, which supports including scroll behavior in the present thesis (hypothesis H1c). Its usage-based approach is also conceptually related to application switching (hypothesis H1d). Second, in both approaches, models trained for each individual performed better than global models. This supports the decision to compare each user with their own personalized baseline instead of applying the same thresholds to everyone. Third, its explicit goal of avoiding privacy-sensitive information matches the privacy requirements of the present thesis.

However, the study targeted stress rather than fatigue, and its gesture-based approach was evaluated only in a laboratory. It is therefore unknown how well such gesture features transfer to uncontrolled everyday use, where scrolling depends strongly on the application and the content being viewed.

## 2.6. Passive Smartphone Sensing and Behavioral Monitoring

Passive smartphone sensing involves collecting behavioral or contextual measurements through mobile devices with limited active participation from users. This approach has been explored in research investigating mental well-being, daily routines, and behavioral changes.

Wang et al. (2014) introduced StudentLife, an Android-based smartphone sensing system designed to investigate mental health, academic performance, and behavioral trends among university students. The researchers monitored 48 Dartmouth College students over a ten-week academic term.

The system collected information related to activity, sleep, mobility, and social behavior. Passive sensing measurements were combined with self-report questionnaires and ecological momentary assessments to investigate relationships between behavioral patterns and participants' experiences.

The study identified significant associations between automatically sensed behavioral measurements and several mental-health and educational outcomes. It also reported changes in stress, sleep, activity, and positive affect across the academic term as students experienced increasing academic demands.

These findings demonstrate the potential of smartphone-based sensing for studying everyday behavior in naturalistic environments. The StudentLife methodology is particularly relevant because it combines passive measurements with participant-reported information.

However, StudentLife investigated broad mental-health and behavioral outcomes rather than short-term mental fatigue inferred from typing, scrolling, and application switching. Its results therefore cannot directly establish the accuracy of the proposed Burnout Detector system.

For the present thesis, StudentLife provides a methodological reference for combining passive smartphone measurements with self-reported fatigue ratings during a participant study.

## 2.7. Android AccessibilityService as a Data Source

None of the studies reviewed above collected data through Android AccessibilityService, the data source used in this thesis. This section describes what the service can provide and the privacy risks that it is known to create.

### 2.7.1. Capabilities and Constraints

Android's accessibility framework allows a service that the user explicitly enables in the system settings to receive events that describe changes in the user interface. Examples are a change of the text in an input field, the scrolling of a view, or a new window coming to the foreground. The framework was designed for assistive tools such as screen readers. Because the system generates these events for applications that use standard Android interface components or implement accessibility support, an accessibility service can observe interaction across most of the device without replacing the user's keyboard. Applications that draw their own interface, such as many games, may send few or no usable events. This property makes it attractive for passive fatigue monitoring.

Accessibility events, however, carry less information than the data sources used in Sections 2.3 to 2.5. A text-change event reports when the text changed and how many characters were added and removed, but not the individual pressing and releasing of keys. The key-press durations and inter-key latencies used in desktop keystroke studies therefore cannot be measured, and typing speed has to be derived from the timing of text-change events. Similarly, scroll events include scroll distances only for some views, depending on the application.

Early device testing in this project confirmed these constraints. Different keyboards reported the same keystroke in different forms, scroll distances were reported by only some applications, and password fields were not always marked as such by the applications that contained them (see [`../month-2/testing-report.md`](../month-2/testing-report.md), Section 8). These observations led to the feature definitions used in this thesis, for example classifying text changes by their net character change rather than by individual keystrokes.

### 2.7.2. Privacy and Security Risks

The same capabilities that make AccessibilityService useful also make it risky. Diao et al. (2019) systematically examined the design of the Android accessibility framework and scanned 91,605 applications from Google Play. They found that the accessibility APIs were widely misused and identified fundamental design shortcomings, including the absence of any restriction on the purposes for which an accessibility service may be used. Naseri et al. (2019) showed that enabling an accessibility service was enough to expose 72% of top finance applications and 80% of top social media applications to eavesdropping on sensitive data such as logins and passwords.

These findings, together with the password-field observation from device testing, shaped the privacy design of the present application (see [`../month-2/privacy-design.md`](../month-2/privacy-design.md)). The application is designed not to request access to window content, not to read the text carried by events, and not to store individual interaction events. Behavioral features are aggregated in memory, and typing-related measurements are suppressed in windows with insufficient activity. Known sensitive contexts, such as system sign-in screens, are excluded where they can be identified. Because the accessibility permission is powerful, explicit consent, monitoring controls, and safe data deletion are treated as core system requirements rather than optional features. Which of these measures have been implemented and verified is reported in the implementation and testing chapters.

## 2.8. Critical Comparison of Existing Research

The reviewed studies demonstrate different but complementary approaches to investigating relationships between computer or smartphone behavior and psychological or cognitive outcomes. Table 2.1 compares them with the present thesis.

**Table 2.1.** Comparison of the reviewed studies with the present thesis.

| Study | Setting and participants | Data source | Interaction features | Ground truth | Main limitation for this thesis |
|---|---|---|---|---|---|
| Vizer et al. (2009) | Laboratory; 24 participants | Physical keyboard | Keystroke timing and frequency (e.g., backspace use); linguistic features | Induced cognitive and physical stress | Laboratory setting; uses text content |
| Epp et al. (2011) | Field (in situ) | Physical keyboard | Key-press durations and inter-key latencies | Experience-sampled emotional states, including tiredness | Requires key-press timing; desktop only |
| Ross et al. (2021) | Field; 19 participants (bipolar disorder and controls) | Custom Android keyboard | Typing speed and other keypress dynamics | Digital Trail Making Test; mood | Replaces the user's keyboard; cognition rather than fatigue |
| Fang et al. (2026) | Field; 366 first-year physicians, about 2 months | Apple SensorKit; wearable sleep data | Typing speed; deletion rate | Time awake (proxy) | Apple only; indirect fatigue measure |
| Visuri et al. (2025) | Field; 2 months | Smartphone typing metrics; Oura ring | Typing speed; error rates | Psychomotor Vigilance Task | Ground truth requires active testing |
| Ciman & Wac (2018) | Laboratory (13) and field (25) | Smartphone interaction and usage data | Tap, scroll, swipe, text input; usage patterns | Stress | Stress rather than fatigue; gestures tested only in the laboratory |
| Wang et al. (2014) | Field; 48 students, 10 weeks | Android sensing application | Activity, sleep, mobility, social behavior | Surveys; ecological momentary assessment | Broad outcomes; no interaction metadata |
| **Present thesis** | Field; 5–10 participants, about 7 days | Android AccessibilityService; user's own keyboard; no text | Typing speed, correction ratio, scroll gestures, application switches (5-minute windows) | KSS prompts | — |

Across these studies, five considerations emerge.

**First, the evidence is strongest for typing speed and correction behavior.** Slower typing was associated with poorer executive function (Ross et al., 2021), prolonged wakefulness (Fang et al., 2026), and poorer vigilance (Visuri et al., 2025). Evidence for correction and error behavior is more mixed. It contributed to desktop stress detection (Vizer et al., 2009) and was correlated with vigilance (Visuri et al., 2025), but deletion rate changed only minimally with time awake (Fang et al., 2026). Evidence for scrolling and usage patterns is limited to stress detection (Ciman & Wac, 2018). This is why hypotheses H1a and H1b are directional, with H1b resting on weaker evidence, whereas H1c and H1d are non-directional.

**Second, the data source determines which features can be measured.** Physical keyboards and custom keyboards provide precise key-press timings. Operating-system sensing frameworks such as SensorKit provide prepared typing metrics but are available only on Apple devices. Android AccessibilityService is available to any Android application without replacing the keyboard, but it provides coarser information and carries significant privacy risks (Diao et al., 2019; Naseri et al., 2019). Custom keyboards, operating-system sensing frameworks, and accessibility services therefore do not provide equivalent information, and results obtained with one cannot be assumed to hold for another.

**Third, fatigue-related ground truth is rarely measured directly.** The reviewed studies used induced stress conditions, emotional self-reports, cognitive tests, time awake, or vigilance tests. Among them, fatigue-related outcomes were measured either through a proxy (time awake) or through an active test (PVT). None related passively collected Android interaction metadata to brief, in-the-moment fatigue ratings.

**Fourth, personalization matters.** Within-subject models outperformed global models in both approaches of Ciman and Wac (2018), and Fang et al. (2026) highlighted individual differences in typing performance. Comparing each user with their own baseline is therefore more promising than applying population-wide thresholds.

**Fifth, associations are not diagnoses.** Smartphone interaction measurements may provide useful information about cognitive and psychological states, but associations should not be interpreted as proof of causation or diagnostic accuracy. Most of the reviewed studies were small or conducted with specific populations, and validation against independently collected participant assessments is necessary before behavioral deviations can be interpreted as indicators of mental fatigue.

These considerations inform the design and evaluation strategy of the present thesis.

## 2.9. Research Gap

The reviewed literature provides evidence that typing behavior can be associated with stress, emotional states including tiredness, cognitive performance, prolonged wakefulness, and vigilance, and that touch interaction and broader passive smartphone sensing can reveal relationships between everyday behavior and well-being.

However, the reviewed studies do not establish whether a lightweight Android application using AccessibilityService alone can reliably collect privacy-conscious interaction features and use personalized behavioral baselines to identify deviations associated with self-reported mental fatigue. Three specific gaps remain, each addressed by one research question (Table 2.2).

1. **Data collection on standard Android devices.** Which typing-related, scrolling, and application-switching features can be reliably extracted from Android accessibility events without retaining textual content, and how consistent is their availability across devices, keyboards, and applications? The reviewed studies used physical keyboards, custom keyboards, SensorKit, or laboratory settings, so this has not been demonstrated.
2. **Association with self-reported fatigue.** Do these coarser, privacy-preserving features show the associations that were found with richer data, when they are compared with each individual's baseline and with in-the-moment fatigue ratings rather than proxies?
3. **Personalized detection.** Can a simple, transparent, rule-based detector that compares each user with their own baseline identify periods of elevated self-reported fatigue better than chance?

In addition, the privacy risks documented by Diao et al. (2019) and Naseri et al. (2019) raise the question of how interaction features can be aggregated into short time windows while preserving user privacy. This question is treated as a design requirement rather than as a research question and was addressed in the system and privacy design.

**Table 2.2.** Mapping of the research gaps to the research questions and hypotheses.

| Gap | Research question | Hypotheses | Main evidence |
|---|---|---|---|
| 1. Data collection on standard Android devices | RQ1 | Descriptive (no hypothesis) | Device testing on at least two manufacturers and several keyboards |
| 2. Association with self-reported fatigue | RQ2 | H1a–H1d | Participant study: features relative to baseline vs. KSS ratings |
| 3. Personalized detection | RQ3 | H2 | Participant study: assessment-level detector predictions vs. KSS ≥ 7, compared with a random baseline |

The present thesis addresses these gaps through the design and implementation of a native Android application using Kotlin, AccessibilityService, and Room for local data storage.

The proposed system will extract feasible interaction features, aggregate them into five-minute time windows, and subsequently compare the resulting measurements with personalized behavioral baselines. A lightweight rule-based anomaly detection approach will be investigated, with its outputs evaluated against participant-reported fatigue levels collected with the Karolinska Sleepiness Scale.

The intended contribution is an exploratory, privacy-conscious Android implementation and an evaluation of its technical feasibility and potential usefulness for mental fatigue monitoring. The application is not intended to diagnose clinical burnout.

## References

Åkerstedt, T., & Gillberg, M. (1990). Subjective and objective sleepiness in the active individual. *International Journal of Neuroscience, 52*(1–2), 29–37. https://doi.org/10.3109/00207459008994241

Boksem, M. A. S., & Tops, M. (2008). Mental fatigue: Costs and benefits. *Brain Research Reviews, 59*(1), 125–139. https://doi.org/10.1016/j.brainresrev.2008.07.001

Ciman, M., & Wac, K. (2018). Individuals' stress assessment using human-smartphone interaction analysis. *IEEE Transactions on Affective Computing, 9*(1), 51–65. https://doi.org/10.1109/TAFFC.2016.2592504

Diao, W., Zhang, Y., Zhang, L., Li, Z., Xu, F., Pan, X., Liu, X., Weng, J., Zhang, K., & Wang, X. (2019). Kindness is a risky business: On the usage of the accessibility APIs in Android. In *Proceedings of the 22nd International Symposium on Research in Attacks, Intrusions and Defenses (RAID 2019)* (pp. 261–275). USENIX Association.

Epp, C., Lippold, M., & Mandryk, R. L. (2011). Identifying emotional states using keystroke dynamics. In *Proceedings of the SIGCHI Conference on Human Factors in Computing Systems* (pp. 715–724). ACM. https://doi.org/10.1145/1978942.1979046

Epstein, D. A., Caraway, M., Johnston, C., Ping, A., Fogarty, J., & Munson, S. A. (2016). Beyond abandonment to next steps: Understanding and designing for life after personal informatics tool use. In *Proceedings of the 2016 CHI Conference on Human Factors in Computing Systems* (pp. 1109–1113). ACM. https://doi.org/10.1145/2858036.2858045

Fang, Y., Yang, P., Frank, E., Goldstein, C., Wright, A. G. C., Bohnert, A. S. B., Kheterpal, V., Sen, S., & Wu, Z. (2026). Patterns of smartphone typing performance by time awake: Implications for unobtrusive ambulatory mental fatigue assessment. *PLOS Digital Health, 5*(3), e0001281. https://doi.org/10.1371/journal.pdig.0001281

Naseri, M., Borges, N. P., Jr., Zeller, A., & Rouvoy, R. (2019). AccessiLeaks: Investigating privacy leaks exposed by the Android accessibility service. *Proceedings on Privacy Enhancing Technologies, 2019*(2), 291–305. https://doi.org/10.2478/popets-2019-0031

Ross, M. K., Demos, A. P., Zulueta, J., Piscitello, A., Langenecker, S. A., McInnis, M., Ajilore, O., Nelson, P. C., Ryan, K. A., & Leow, A. (2021). Naturalistic smartphone keyboard typing reflects processing speed and executive function. *Brain and Behavior, 11*(11), e2363. https://doi.org/10.1002/brb3.2363

Visuri, A., Koskimäki, H., van Berkel, N., Alorwu, A., Peltonen, E., Abdullah, S., & Hosio, S. (2025). Cognitive performance measurements and the impact of sleep quality using wearable and mobile sensors. *Personal and Ubiquitous Computing, 29*, 193–210. https://doi.org/10.1007/s00779-025-01847-7

Vizer, L. M., Zhou, L., & Sears, A. (2009). Automated stress detection using keystroke and linguistic features: An exploratory study. *International Journal of Human-Computer Studies, 67*(10), 870–886. https://doi.org/10.1016/j.ijhcs.2009.07.005

Wang, R., Chen, F., Chen, Z., Li, T., Harari, G. M., Tignor, S., Zhou, X., Ben-Zeev, D., & Campbell, A. T. (2014). StudentLife: Assessing mental health, academic performance and behavioral trends of college students using smartphones. In *Proceedings of the 2014 ACM International Joint Conference on Pervasive and Ubiquitous Computing* (pp. 3–14). ACM. https://doi.org/10.1145/2632048.2632054

World Health Organization. (2019, May 28). *Burn-out an "occupational phenomenon": International Classification of Diseases*. https://www.who.int/news-room/detail/28-05-2019-burn-out-an-occupational-phenomenon-international-classification-of-diseases
