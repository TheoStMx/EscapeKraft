# EscapeKraft Alignment Roadmap

This document keeps track of the main alignment gaps identified during code review.

The goal is to fix them step by step, with one focused change set and one commit per step whenever possible.

## Working Rule

For each step:

1. implement the change
2. re-check compilation
3. review the impact on the rest of the codebase
4. create one dedicated commit

## Priority Steps

### Step 1. Restore Encapsulation In Teams And Scores

Main issue:

- internal mutable state is exposed too directly
- team fields are public
- score lists can be modified from outside their owning classes

Files concerned:

- `src/main/java/fr/ekrec/teams/EKTeam.java`
- `src/main/java/fr/ekrec/teams/EKTeamManager.java`
- `src/main/java/fr/ekrec/scores/EKPersistentState.java`
- `src/main/java/fr/ekrec/scores/EKScoreManager.java`

Expected alignment:

- make fields private where possible
- expose getters or behavior methods instead of raw field access
- return defensive copies or unmodifiable views where needed
- preserve invariants inside the owning classes

### Step 2. Centralize Time And Player-Facing Formatting

Main issue:

- timer formatting is duplicated
- live timer and final score formatting are not aligned
- some player-facing text patterns are still hardcoded in Java logic

Files concerned:

- `src/main/java/fr/ekrec/teams/EKTeamManager.java`
- `src/main/java/fr/ekrec/scores/EKScore.java`
- `src/main/java/fr/ekrec/leaderboard/EKLeaderboardDisplayManager.java`
- `src/main/resources/assets/escapekraft/lang/en_us.json`
- `src/main/resources/assets/escapekraft/lang/fr_fr.json`

Expected alignment:

- define one canonical time format strategy
- reuse shared formatting logic
- move player-visible strings out of Java when practical
- keep output consistent across timer, score, chat leaderboard, and display leaderboard

### Step 3. Refactor Leaderboard Display Rendering

Main issue:

- leaderboard displays are built through manual NBT string assembly
- rendering logic is too coupled to line parsing conventions
- the current approach is brittle for future changes

Files concerned:

- `src/main/java/fr/ekrec/leaderboard/EKLeaderboardDisplayManager.java`
- `src/main/java/fr/ekrec/leaderboard/EKLeaderboardDisplayState.java`
- `src/main/java/fr/ekrec/leaderboard/EKLeaderboardDisplayConfig.java`

Expected alignment:

- separate leaderboard data from display rendering
- avoid reparsing strings to recover rank, team name, and time
- reduce fragile manual NBT generation
- keep the implementation easier to evolve later

### Step 4. Clarify The Status Of Placeholder Mixins

Main issue:

- placeholder mixins are still active even though they do nothing

Files concerned:

- `src/main/java/fr/ekrec/mixin/ExampleMixin.java`
- `src/client/java/fr/ekrec/mixin/client/ExampleClientMixin.java`
- `src/main/resources/escapekraft.mixins.json`
- `src/client/resources/escapekraft.client.mixins.json`
- `src/main/resources/fabric.mod.json`

Expected alignment:

- decide whether these mixins stay temporarily, get disabled, or are removed later
- avoid treating placeholder runtime code as if it were real feature code
- document the choice clearly

### Step 5. Clean Structural Noise And Dead Code

Main issue:

- there are still unused imports, unused locals, and a few leftover template-style traces

Files likely concerned:

- `src/main/java/fr/ekrec/EscapeKraft.java`
- `src/main/java/fr/ekrec/teams/EKTeamManager.java`
- `src/main/java/fr/ekrec/commands/EKCommands.java`
- other files touched during cleanup

Expected alignment:

- remove unused imports and locals
- keep logs intentional and useful
- reduce low-value noise in the codebase

### Step 6. Tighten Version And Dependency Alignment

Main issue:

- some version declarations are still too permissive or not pinned cleanly enough

Files concerned:

- `gradle.properties`
- `build.gradle`
- `src/main/resources/fabric.mod.json`

Expected alignment:

- use stable and explicit tool versions when possible
- make compatibility expectations clearer
- reduce ambiguity around Minecraft, Fabric Loader, Fabric API, and Loom alignment

## Lower Priority Follow-Up

These are not immediate blockers, but should stay visible:

- keep command behavior predictable for edge cases
- continue separating command parsing from business logic when features grow
- validate persistence changes carefully before altering save formats
- re-check side separation if client-only features are added later
- revisit datagen only when the amount of content justifies it

## Session Goal

After each step, the codebase should be:

- cleaner than before
- more coherent with the project conventions
- easier to extend or modify later
- safer to maintain across future feature work
