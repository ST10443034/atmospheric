# AI tool use and citations

**Module:** OPSC6312 · **Project:** Atmospheric (Group 24) · **Part 3**

## Tool used

Claude (Anthropic), used through the Claude Code coding assistant,
Anthropic (2026). Available at: https://www.anthropic.com/claude-code.

## How it was used

- **Code generation.** It helped write the Part 3 features: unit conversion
  (`UnitSystem`), severe-weather alert rules, the notification channel,
  Firebase Cloud Messaging service, WorkManager sync worker, Firestore profile
  repository, Google Sign-In wiring and the profile-photo resizing helper.
- **Fixing lecturer feedback.** It replaced the placeholder example unit test
  with real unit tests, and removed the hard-coded °C / km/h displays.
- **Debugging.** It was used to read Gradle output and Android Lint results
  and suggest fixes for build errors.
- **Translations.** The isiZulu and Afrikaans strings for the new features
  were machine-assisted and are flagged in the repository as unverified.
- **Documentation.** It drafted the README sections and release notes from the
  project's code and the assignment brief.

## What stayed with the team

- Choosing the app idea, the design, the Firebase project and the data model.
- Reviewing, building and running the generated code, and testing on a
  physical phone.
- Recording the demonstration video and making the final submission.

## Sources used

- Open-Meteo Forecast API documentation: https://open-meteo.com/en/docs
- Firebase Cloud Messaging: https://firebase.google.com/docs/cloud-messaging
- Android WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager
