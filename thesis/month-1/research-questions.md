# Research Questions

## Research Questions

**RQ1:** To what extent can Android AccessibilityService provide measurable interaction metadata for behavioral analysis without recording the content of typed text, and how consistent is this metadata across device manufacturers and keyboard applications?

**RQ2:** Which smartphone interaction features, such as typing-related activity, correction behavior, scrolling variability, and application switching frequency, show associations with self-reported mental fatigue?

**RQ3:** Can a lightweight, personalized, rule-based anomaly detection approach identify behavioral deviations associated with elevated self-reported mental fatigue?

RQ1 is a descriptive feasibility question. It is answered through technical implementation and device testing rather than through hypothesis testing. RQ2 and RQ3 are addressed by the hypotheses below.

## Research Hypotheses

All hypotheses are tested within individuals, relative to each participant's personalized baseline. Given the small planned sample (5–10 participants over approximately 7 days), the analysis is exploratory.

### Hypotheses for RQ2 (feature associations)

**H1a:** Typing speed is lower than an individual's baseline during periods of elevated self-reported mental fatigue.
*H0a:* Typing speed does not differ from baseline during periods of elevated self-reported mental fatigue.

**H1b:** The deletion-based correction ratio is higher than an individual's baseline during periods of elevated self-reported mental fatigue.
*H0b:* The deletion-based correction ratio does not differ from baseline during periods of elevated self-reported mental fatigue.

**H1c:** Scroll variability differs from an individual's baseline during periods of elevated self-reported mental fatigue.
*H0c:* Scroll variability does not differ from baseline during periods of elevated self-reported mental fatigue.

**H1d:** Application switching frequency differs from an individual's baseline during periods of elevated self-reported mental fatigue.
*H0d:* Application switching frequency does not differ from baseline during periods of elevated self-reported mental fatigue.

H1a and H1b are directional. H1a follows findings linking slower smartphone typing to poorer executive function, prolonged wakefulness, and lower vigilance (Ross et al., 2021; Fang et al., 2026; Visuri et al., 2025). H1b follows findings linking correction- and error-related typing behavior to stress and lower vigilance (Vizer et al., 2009; Visuri et al., 2025). The evidence for H1b is weaker than for H1a: Fang et al. (2026) found that deletion rate changed only minimally with time awake. H1c and H1d are non-directional because prior evidence for these features is limited. Full references are listed in [`literature-review.md`](literature-review.md).

### Hypothesis for RQ3 (detection)

**H2:** The rule-based anomaly detector identifies assessment periods associated with elevated self-reported fatigue with higher precision and recall than a random baseline that flags the same proportion of eligible assessment periods.
*H0-2:* The detector performs no better than the random baseline.

## Operational Definitions

- **Self-reported fatigue indicator:** measured with the Karolinska Sleepiness Scale (KSS; 1 = extremely alert, 9 = very sleepy, fighting sleep). The KSS measures subjective sleepiness and is used in this study as a proxy for the reduced alertness associated with short-term mental fatigue. Ratings are collected through in-app prompts approximately 3–5 times per day.
- **Elevated fatigue:** a KSS rating of 7 or higher. A participant-relative definition (a rating above the participant's own median) will be reported as a sensitivity analysis. Both rules are fixed before data analysis.
- **Deletion-based correction ratio:** deletions ÷ (typed characters + deletions) within a five-minute window, where typed characters and deletions are text changes with a net character change of +1 and −1 respectively. Multi-character replacements (e.g., autocorrect) are excluded.
- **Linking ratings to behavior:** each self-report is associated with the five-minute interaction windows whose end timestamps fall within the 30 minutes before the response, that is, in the interval (response time − 30 min, response time]. The lower boundary is excluded and the response timestamp is included, so each rating is linked to at most six clock-aligned windows. Complete windows are used in the primary analysis, while incomplete windows (for example, windows in which monitoring was paused or stopped) may be examined separately in a sensitivity analysis.
- **Assessment period:** the 30-minute period linked to one answered KSS prompt. An assessment period is eligible for evaluating H2 when it contains at least one complete window and lies after the baseline period. The rule that combines the window-level anomaly flags of an assessment period into one prediction (for example, a minimum proportion of flagged windows) will be fixed before evaluation.
- **Personalized baseline:** per-participant feature statistics (e.g., median and spread) computed during an initial observation period.

## Planned Analysis

Because each participant contributes many repeated measurements, associations (H1a–d) will be examined with within-person methods such as linear mixed-effects models or repeated-measures correlation. Detector performance (H2) will be reported at the assessment level using precision, recall and a confusion matrix.

Personalized baselines are calculated from an initial observation period, and windows from that period are not used to evaluate the detector. Detector thresholds are established before evaluation or refined using separate calibration data (for example, through leave-one-participant-out validation). The same observations are never used both to optimize thresholds and to evaluate detection performance, because doing so would artificially inflate the detector's measured performance.

## Research Objectives

1. Review existing scientific literature on mental fatigue, keystroke dynamics, and mobile interaction analysis.
2. Investigate the technical feasibility of collecting interaction metadata through Android AccessibilityService.
3. Compare feature availability across at least two device manufacturers (e.g., Samsung and Google Pixel) and keyboard applications.
4. Design and implement a privacy-conscious Android application using Kotlin and Room.
5. Extract measurable interaction features using five-minute time windows.
6. Develop a personalized baseline and rule-based anomaly detection approach.
7. Evaluate the system through device testing and a participant study, analyzing interaction features against self-reported fatigue using within-person statistical methods.
