# Problem Statement

## 1. Background and Motivation

Mental fatigue is a state that can arise after prolonged, demanding cognitive activity and may influence attention, performance, and behavior. Boksem and Tops (2008) describe it as the point at which the perceived energetic costs of continuing a task outweigh its expected rewards, so that people are no longer motivated to keep performing. Short-term fatigue of this kind is a normal response, but when demanding work continues without recovery, it can become part of a longer-term problem.

Burnout is the most prominent example of such a longer-term problem. The World Health Organization includes burn-out in ICD-11 as an occupational phenomenon resulting from chronic workplace stress that has not been successfully managed, characterized by exhaustion, increased mental distance from one's job, and reduced professional efficacy; it is not classified as a medical condition (World Health Organization, 2019). This thesis does not attempt to detect or diagnose burnout. It focuses on **short-term mental fatigue**, which is measurable within a day and can serve as an early warning sign that recovery is needed.

Smartphones are used throughout the day for communication, studying, and work. Every interaction, such as typing, correcting, scrolling, and switching between applications, produces measurable behavior. Changes in these interaction patterns may therefore provide information about a user's cognitive state without requiring the user to do anything extra.

## 2. Problem

Established methods for assessing mental fatigue require active participation. Self-report questionnaires interrupt the user, and objective tests such as the Psychomotor Vigilance Task require several minutes of focused effort each time they are taken (Fang et al., 2026). Physiological measurements usually require additional equipment. These methods are valuable in studies, but they are poorly suited to continuous monitoring in everyday life.

Effortful self-monitoring is also difficult to sustain. Research on personal informatics shows that people commonly lapse in or abandon self-tracking tools over time (Epstein et al., 2016). A fatigue-awareness tool that depends on regular manual input is therefore likely to be used for only a short period, which is exactly when early warning signs would be missed.

A passive approach, which observes how a person uses their smartphone rather than asking them, could avoid both problems. However, it raises two new challenges: whether the necessary behavioral signals can be measured reliably on ordinary smartphones, and whether this can be done without collecting private content such as typed text.

## 3. Research Gap

Existing studies show that smartphone interaction behavior is related to cognitive state. Naturalistic typing speed has been associated with processing speed and executive function (Ross et al., 2021), typing speed varies with time awake (Fang et al., 2026), typing metrics have been related to alertness measured with the Psychomotor Vigilance Task (Visuri et al., 2025), and touch interactions such as scrolling, swiping, and text input have been used to assess stress (Ciman & Wac, 2018). Broader passive sensing has also been linked to students' mental health and academic outcomes (Wang et al., 2014).

However, these studies relied on data collection methods that are not available to a typical Android application: custom keyboard applications that replace the user's keyboard (Ross et al., 2021), an operating-system sensing framework available only on Apple devices (Fang et al., 2026), or controlled laboratory sessions (Ciman & Wac, 2018). It therefore remains unclear:

- whether comparable typing, correction, scrolling, and application-switching features can be obtained on standard Android devices through **AccessibilityService alone**, without replacing the user's keyboard and without reading or retaining typed text;
- how consistent these features are across device manufacturers, keyboards, and applications; and
- whether **personalized deviations** in these features correspond to **self-reported mental fatigue** in everyday use.

Early device testing in this project already showed that this gap is real. Different keyboards report the same keystroke in different forms, scroll distances are reported by only some applications, and password fields are not always marked as such by the applications that contain them (see `thesis/month-2/testing-report.md`, Section 8). Feasibility and privacy therefore have to be investigated, not assumed.

## 4. Aim and Approach

The aim of this thesis is to develop and evaluate a privacy-conscious Android application that passively detects behavioral indicators of short-term mental fatigue.

The application uses Android's AccessibilityService to observe interaction events and extracts only numeric metadata: typing speed, deletion-based correction ratio, scroll gestures, and application switches. Event text is never read or stored. Features are aggregated in memory into fixed five-minute windows, and only the window summaries are stored locally on the device. After an initial observation period, each user's typical behavior forms a personalized baseline, and a lightweight rule-based anomaly detector flags deviations from it. These flags are evaluated against ratings that participants enter in the application using the Karolinska Sleepiness Scale, a measure of sleepiness that is used here as a proxy for the reduced alertness associated with short-term mental fatigue.

The application is intended as an exploratory fatigue-awareness tool, not a clinical or diagnostic system.

## 5. Research Questions

**RQ1:** To what extent can Android AccessibilityService provide measurable interaction metadata for behavioral analysis without recording the content of typed text, and how consistent is this metadata across device manufacturers and keyboard applications?

**RQ2:** Which smartphone interaction features, such as typing-related activity, correction behavior, scrolling variability, and application switching frequency, show associations with self-reported mental fatigue?

**RQ3:** Can a lightweight, personalized, rule-based anomaly detection approach identify behavioral deviations associated with elevated self-reported mental fatigue?

RQ2 is addressed by hypotheses H1a–H1d (typing speed, correction ratio, scroll variability, and application switching relative to the personal baseline), and RQ3 by hypothesis H2 (detection performance compared with a random baseline). The hypotheses and operational definitions are specified in [`research-questions.md`](research-questions.md).

## 6. Scope and Limitations

- **Platform.** The application targets Android 9 (API 28) and later and operates on locally stored, aggregated behavioral data only. It does not capture the textual content entered by users.
- **Device and keyboard differences.** The availability and accuracy of interaction features depend on the Android version, device manufacturer, keyboard application, and the accessibility events each application sends. Device testing has confirmed such differences.
- **Privacy limits.** Password detection is best-effort, because some applications do not mark their password fields. The design mitigates this by never storing individual events, but the risk cannot be removed completely.
- **Fatigue, not burnout.** The research focuses on possible indicators of short-term mental fatigue. It will not attempt to diagnose clinical burnout or any other medical condition.
- **Ground truth.** Self-reported ratings are subjective and may be noisy, and participants may not answer every prompt. The KSS measures sleepiness, which overlaps with mental fatigue but is not identical to it.
- **Detection thresholds.** Anomaly thresholds are defined heuristically before evaluation. Any refinement uses separate calibration data, never the observations used to evaluate the detector. Because of the small sample, results remain tentative.
- **Sample size.** A small participant study (5–10 participants over approximately seven days) is used for exploratory evaluation, subject to the necessary consent and institutional approvals. Results will not support conclusions about the general population.
- **Installation.** The application is installed directly as an APK rather than through Google Play. Depending on the Android version and the installation method, participants may first have to allow "restricted settings" before they can enable the accessibility service. On Android 13 and later, this typically applies to applications installed directly from an APK file rather than through an app store. This adds a setup step and may affect recruitment.

## References

Boksem, M. A. S., & Tops, M. (2008). Mental fatigue: Costs and benefits. *Brain Research Reviews, 59*(1), 125–139. https://doi.org/10.1016/j.brainresrev.2008.07.001

Ciman, M., & Wac, K. (2018). Individuals' stress assessment using human-smartphone interaction analysis. *IEEE Transactions on Affective Computing, 9*(1), 51–65.

Epstein, D. A., Caraway, M., Johnston, C., Ping, A., Fogarty, J., & Munson, S. A. (2016). Beyond abandonment to next steps: Understanding and designing for life after personal informatics tool use. In *Proceedings of the 2016 CHI Conference on Human Factors in Computing Systems* (pp. 1109–1113). ACM. https://doi.org/10.1145/2858036.2858045

Fang, Y., Yang, P., Frank, E., Goldstein, C., Wright, A. G. C., Bohnert, A. S. B., Kheterpal, V., Sen, S., & Wu, Z. (2026). Patterns of smartphone typing performance by time awake: Implications for unobtrusive ambulatory mental fatigue assessment. *PLOS Digital Health, 5*(3), e0001281. https://doi.org/10.1371/journal.pdig.0001281

Ross, M. K., Demos, A. P., Zulueta, J., Piscitello, A., Langenecker, S. A., McInnis, M., Ajilore, O., Nelson, P. C., Ryan, K. A., & Leow, A. (2021). Naturalistic smartphone keyboard typing reflects processing speed and executive function. *Brain and Behavior, 11*(11), e2363. https://doi.org/10.1002/brb3.2363

Visuri, A., Koskimäki, H., van Berkel, N., Alorwu, A., Peltonen, E., Abdullah, S., & Hosio, S. (2025). Cognitive performance measurements and the impact of sleep quality using wearable and mobile sensors. *Personal and Ubiquitous Computing, 29*, 193–210. https://doi.org/10.1007/s00779-025-01847-7

Wang, R., Chen, F., Chen, Z., Li, T., Harari, G. M., Tignor, S., Zhou, X., Ben-Zeev, D., & Campbell, A. T. (2014). StudentLife: Assessing mental health, academic performance and behavioral trends of college students using smartphones. In *Proceedings of the 2014 ACM International Joint Conference on Pervasive and Ubiquitous Computing* (pp. 3–14). ACM. https://doi.org/10.1145/2632048.2632054

World Health Organization. (2019, May 28). *Burn-out an "occupational phenomenon": International Classification of Diseases*. https://www.who.int/news-room/detail/28-05-2019-burn-out-an-occupational-phenomenon-international-classification-of-diseases
