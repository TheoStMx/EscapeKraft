# EscapeKraft Code Review Checklist

This document is used after a development session to realign the codebase with the project's technical principles.

It is not meant to block progress on small experiments. Its purpose is to catch drift early and keep the mod maintainable as features are added, changed, or removed.

## How To Use

Review the changes made during the session and check this list before considering the work aligned.

Questions to ask during review:

- Is the code correct for the current feature?
- Is the code coherent with the rest of the mod?
- Is the code easy to extend, modify, or remove later?
- Does the code still match the Minecraft and Fabric versions targeted by the project?

## Core Principles

- Prefer clear and boring code over clever code.
- Keep responsibilities small and explicit.
- Use the Minecraft and Fabric APIs directly when they already provide the needed behavior.
- Avoid unnecessary abstractions, but also avoid hardcoded duplication that will spread.
- Keep future feature changes cheap by reducing coupling and isolating responsibilities.

## Review Checklist

### 1. Correctness

- The feature works as intended in the current gameplay or technical flow.
- Edge cases introduced by the change were identified and handled.
- Failure paths return a safe result, a clear message, or both.
- The code does not silently ignore important errors without a reason.

### 2. DRY And Reuse

- Repeated logic was extracted when the duplication is real and likely to grow.
- Shared formatting, validation, message building, and lookup logic are not copy-pasted across classes.
- Common behavior is centralized only when that improves readability and maintenance.
- Over-abstraction was avoided: do not extract code only to satisfy a theoretical reuse case.

### 3. Encapsulation

- Internal state is not exposed more than necessary.
- Fields are kept `private` unless wider visibility is justified.
- Collections are not exposed in a way that allows uncontrolled mutation from outside.
- Classes expose behavior-oriented methods instead of forcing callers to manipulate raw internals.
- Invariants are preserved by the owning class.

### 4. Structure And Cohesion

- Each class has one main responsibility.
- Packages stay coherent with their purpose: commands, teams, scores, leaderboard, items, sounds, and so on.
- Utility code is not mixed into domain classes without a clear reason.
- Server logic, client logic, and shared logic remain separated.
- Temporary code added during development was removed or clearly isolated.

### 5. Consistency With The Existing Codebase

- Naming is coherent with the rest of the project.
- New code follows the same style and level of abstraction as nearby code.
- The solution does not introduce a different pattern when an existing project pattern already fits.
- Existing helper methods or managers were reused where appropriate.

### 6. Minecraft Modding Practices

- The implementation uses the correct side: server-only, client-only, or shared.
- Registrations happen in the proper initialization phase.
- Commands, persistent state, resources, and world interactions follow normal Fabric and Minecraft usage patterns.
- Mixins are only used when the API or event system is insufficient.
- New logic does not depend on unstable or internal behavior unless there is no better option.
- Text shown to players uses `Text` APIs correctly and avoids raw hardcoded strings in code.

### 7. Version Alignment

- The code is aligned with the Minecraft version declared by the project.
- The code is aligned with the Fabric Loader and Fabric API versions used by the project.
- APIs, mappings, and signatures used in the code match the current target version.
- Old examples, outdated tutorials, or snippets from other versions were not copied blindly.
- If a version-sensitive choice was made, it is documented in the code or commit context.

### 8. Extensibility And Change Readiness

- The feature can be modified later without rewriting unrelated code.
- Adding a new command, score type, display mode, team rule, or resource would not require invasive edits everywhere.
- Removing the feature later would be reasonably localized.
- Constants, translation keys, identifiers, and shared rules are not scattered across the codebase.
- Hardcoded assumptions are limited and explicit.

### 9. Localization And Text Rules

- Player-facing text goes through `lang` files.
- `en_us.json` is updated when a new translation key is introduced.
- Other maintained language files are updated in the same session when relevant.
- Translation keys use a consistent naming scheme.
- Debug, admin, and gameplay messages are not mixed carelessly.

### 10. Naming And Language Conventions

- Code, comments, and technical documentation are written in English.
- Translation content may be localized in `lang` files, but not hardcoded in Java.
- Class, method, field, package, and key names are explicit and consistent.
- Abbreviations are kept limited and understandable.

### 11. Resource And Data Hygiene

- Resource paths, identifiers, and translation keys are consistent and correctly namespaced.
- JSON resources follow the expected structure for the targeted version.
- Generated-looking data is not manually duplicated in many files without reason.
- Saved data structures are stable enough to survive future updates or are clearly marked as temporary.

### 12. Persistence And Compatibility

- Persistent data changes are backward-aware when possible.
- Save format changes are reviewed carefully, especially for `PersistentState`, codecs, identifiers, and keys.
- Renaming keys, fields, or resource identifiers is done intentionally.
- Risky changes to persisted data are documented.

### 13. Commands And UX

- Commands have clear permissions and expected callers.
- Feedback messages are explicit enough for players, operators, or command blocks.
- Admin-only operations are protected appropriately.
- Command behavior is predictable for missing data, invalid targets, and repeated execution.

### 14. Performance And Tick Safety

- Tick-time logic stays lightweight.
- Expensive lookups, repeated allocations, and repeated world scans are justified.
- Per-player or per-tick behavior is reviewed with multiplayer scale in mind.
- The change does not introduce obvious memory or entity leaks.

### 15. Testing And Validation

- The project still compiles after the change.
- Existing behavior impacted by the change was rechecked.
- New commands, saved data paths, and user-visible flows were validated when relevant.
- If something could not be tested, the gap is explicitly noted.

### 16. Logging And Debuggability

- Logs are useful and not noisy.
- Errors that matter leave enough context to investigate.
- Debug-only traces are not left in normal runtime paths unless intentionally kept.

## Common Review Triggers

Use this checklist especially after:

- adding a new gameplay feature
- changing command behavior
- introducing or modifying persistence
- adding client-specific logic
- adding mixins
- touching multiple packages in one session
- copying code from examples, tutorials, or previous mods

## Typical Smells To Catch

- hardcoded player-facing text in Java
- public mutable fields or collections
- command logic mixed directly with storage details
- duplicated formatting or lookup code
- direct use of version-specific snippets without verification
- client classes referenced from server code
- one class doing command parsing, state management, persistence, and display formatting at once
- placeholder or debug code left behind

## Session Close-Out

Before ending a session, verify at least:

- the code compiles
- translation keys are synced
- the structure still makes sense
- no obvious template, placeholder, or dead code was introduced
- the new code matches the project's conventions

If the answer is "not yet" for any important item, note it explicitly and keep the debt localized and visible.
