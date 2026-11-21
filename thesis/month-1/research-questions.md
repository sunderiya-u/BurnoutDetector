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

**H1b:** Backspace ratio is higher than an individual's baseline during periods of elevated self-reported mental fatigue.
*H0b:* Backspace ratio does not differ from baseline during periods of elevated self-reported mental fatigue.

**H1c:** Scroll variability differs from an individual's baseline during periods of elevated self-reported mental fatigue.
*H0c:* Scroll variability does not differ from baseline during periods of elevated self-reported mental fatigue.

**H1d:** Application switching frequency differs from an individual's baseline during periods of elevated self-reported mental fatigue.
*H0d:* Application switching frequency does not differ from baseline during periods of elevated self-reported mental fatigue.

H1a and H1b are directional, following prior findings linking slower typing and correction behavior to reduced alertness and cognitive performance (Fang et al., 2026; Ross et al., 2021). H1c and H1d are non-directional because prior evidence for these features is limited.

### Hypothesis for RQ3 (detection)

**H2:** The rule-based anomaly detector identifies periods of elevated self-reported mental fatigue with higher precision and recall than a random baseline that flags the same proportion of time windows.
*H0-2:* The detector performs no better than the random baseline.

## Operational Definitions

- **Self-reported mental fatigue:** measured with an in-app rating prompt shown several times per day (planned: 3–5 prompts). The scale (e.g., Karolinska Sleepiness Scale or a 0–10 visual analogue scale) will be selected and justified in the study design.
- **Elevated fatigue:** a rating at or above a predefined threshold (e.g., KSS ≥ 7) or above the participant's own median rating. The final rule will be fixed before data analysis.
- **Linking ratings to behavior:** each self-report is compared with the 5-minute interaction windows recorded in the 30 minutes preceding the rating.
- **Personalized baseline:** per-participant feature statistics (e.g., median and spread) computed during an initial observation period.

## Planned Analysis

Because each participant contributes many repeated measurements, associations (H1a–d) will be examined with within-person methods such as linear mixed-effects models or repeated-measures correlation. Detector performance (H2) will be reported using precision, recall and a confusion matrix.

## Research Objectives

1. Review existing scientific literature on mental fatigue, keystroke dynamics, and mobile interaction analysis.
2. Investigate the technical feasibility of collecting interaction metadata through Android AccessibilityService.
3. Compare feature availability across at least two device manufacturers (e.g., Samsung and Google Pixel) and keyboard applications.
4. Design and implement a privacy-conscious Android application using Kotlin and Room.
5. Extract measurable interaction features using five-minute time windows.
6. Develop a personalized baseline and rule-based anomaly detection approach.
7. Evaluate the system through device testing and a participant study, analyzing interaction features against self-reported fatigue using within-person statistical methods.
