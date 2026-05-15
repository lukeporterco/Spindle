# Project Stats

Last updated: 2026-05-15 @11am

This file is a lightweight repository shape snapshot. It is descriptive, not a quality score.

## Scope

Counts are based on the current repository snapshot. LOC means physical text lines, including blank lines and comments. Binary files are counted in file totals but excluded from LOC.

Excluded when present: `.git/`, Gradle caches, build output, top-level `runtime/`, `.tools/`, IDE metadata, OS clutter, downloaded dependencies, and generated runtime artifacts.

The only binary file in this snapshot is `gradle/wrapper/gradle-wrapper.jar`; it is counted as a file and excluded from LOC.

## Summary

- Total LOC: 92,712
- Total files in snapshot: 730
- Text files counted for LOC: 729
- Binary files counted but excluded from LOC: 1
- Gradle modules: 9
- Top-level project areas: 12 plus root-level files
- Main source LOC: 47,986
- Test source LOC: 36,715
- Test-to-main LOC ratio: 0.77:1
- Java packages: 70
- JUnit test methods counted by `@Test`: 705

Gradle modules from `settings.gradle.kts`:

- `spindle-loader-api`
- `spindle-loader-core`
- `spindle-loader-cli`
- `target-minecraft`
- `sample-game`
- `sample-mod`
- `sample-runtime-mod`
- `sample-server-fixture`
- `sample-minecraft-mod`

## LOC by area

| Area | Files | Text files | LOC | Share of LOC |
|---|---:|---:|---:|---:|
| `target-minecraft` | 445 | 445 | 60,936 | 65.7% |
| `spindle-loader-core` | 165 | 165 | 20,543 | 22.2% |
| `docs` | 62 | 62 | 4,403 | 4.7% |
| `spindle-loader-cli` | 11 | 11 | 2,811 | 3.0% |
| `[root]` | 10 | 10 | 2,416 | 2.6% |
| `backlog` | 2 | 2 | 818 | 0.9% |
| `spindle-loader-api` | 20 | 20 | 501 | 0.5% |
| `sample-runtime-mod` | 3 | 3 | 98 | 0.1% |
| `sample-server-fixture` | 2 | 2 | 83 | 0.1% |
| `sample-minecraft-mod` | 3 | 3 | 51 | 0.1% |
| `sample-mod` | 3 | 3 | 33 | 0.0% |
| `sample-game` | 2 | 2 | 12 | 0.0% |
| `gradle` | 2 | 1 | 7 | 0.0% |

## Source and test split

| Bucket | Files | Text files | LOC | Java files | Java LOC |
|---|---:|---:|---:|---:|---:|
| `src/main` | 519 | 519 | 47,986 | 516 | 47,906 |
| `src/test` | 122 | 122 | 36,715 | 118 | 36,513 |
| `docs` | 62 | 62 | 4,403 | 0 | 0 |
| `backlog` | 2 | 2 | 818 | 0 | 0 |
| `other/root/config` | 25 | 24 | 2,790 | 0 | 0 |

Test source is 76.5% of main source by LOC. Java-only test source is 76.2% of main Java source by LOC.

## Source and test split by Gradle module

| Module | `src/main` LOC | `src/test` LOC | Test/main ratio | Main Java files | Test Java files | Test classes | Test methods |
|---|---:|---:|---:|---:|---:|---:|---:|
| `spindle-loader-api` | 491 | 0 | 0.00:1 | 18 | 0 | 0 | 0 |
| `spindle-loader-core` | 13,233 | 7,290 | 0.55:1 | 146 | 17 | 16 | 144 |
| `spindle-loader-cli` | 1,573 | 1,214 | 0.77:1 | 7 | 2 | 2 | 32 |
| `target-minecraft` | 32,451 | 28,211 | 0.87:1 | 340 | 99 | 93 | 529 |
| `sample-game` | 9 | 0 | 0.00:1 | 1 | 0 | 0 | 0 |
| `sample-mod` | 26 | 0 | 0.00:1 | 1 | 0 | 0 | 0 |
| `sample-runtime-mod` | 91 | 0 | 0.00:1 | 1 | 0 | 0 | 0 |
| `sample-server-fixture` | 68 | 0 | 0.00:1 | 1 | 0 | 0 | 0 |
| `sample-minecraft-mod` | 44 | 0 | 0.00:1 | 1 | 0 | 0 | 0 |

No separate integration-test source set was detected. Integration-style tests, if any, are counted under `src/test`.

## File types

| Type | Files | Text files | LOC |
|---|---:|---:|---:|
| `.java` | 634 | 634 | 84,419 |
| `.md` | 70 | 70 | 6,598 |
| `.kts` | 11 | 11 | 1,173 |
| `.json` | 7 | 7 | 282 |
| `[no extension]` | 5 | 5 | 220 |
| `.bat` | 1 | 1 | 13 |
| `.jar` | 1 | 0 | 0 |
| `.properties` | 1 | 1 | 7 |

## Java package inventory

- Java source files: 634
- Java LOC: 84,419
- Unique declared Java packages: 70
- Java files without a package declaration: 0

| Namespace group | Packages | Java files | Java LOC |
|---|---:|---:|---:|
| `com.spindle.core.*` | 56 | 606 | 83,483 |
| `com.spindle.api.*` | 6 | 18 | 491 |
| `com.spindle.fixture.*` | 2 | 4 | 252 |
| `com.spindle.sample*` | 5 | 5 | 158 |
| `net.minecraft.*` | 1 | 1 | 35 |

## Test inventory

- `src/test/java` files: 118
- Test classes named `*Test.java`: 111
- Non-test fixture Java files under `src/test/java`: 7
- JUnit `@Test` methods: 705

| Module | `src/test/java` files | Test classes | Test methods | Test Java LOC |
|---|---:|---:|---:|---:|
| `target-minecraft` | 99 | 93 | 529 | 28,009 |
| `spindle-loader-core` | 17 | 16 | 144 | 7,290 |
| `spindle-loader-cli` | 2 | 2 | 32 | 1,214 |

Test method counts are annotation-based and currently count JUnit `@Test` annotations. They do not infer generated, dynamic, parameterized, or externally supplied test cases beyond visible annotations.

## Documentation inventory

- Markdown files total: 70
- Markdown LOC total: 6,598
- `docs/` Markdown files: 62
- `docs/` LOC: 4,403
- `docs/architecture/` Markdown files: 52
- `docs/architecture/` LOC: 3,439
- `docs/mods/` Markdown files: 9
- `docs/mods/` LOC: 956
- Architecture README files: 12
- Architecture non-README, non-template docs: 38
- Target pass docs named `target-*.md`: 28
- Architecture templates: 2
- `backlog/` Markdown files: 2
- `backlog/` LOC: 818
- Root/module Markdown files outside `docs/` and `backlog/`: 6
- Root/module Markdown LOC outside `docs/` and `backlog/`: 1,377

### Architecture docs by area

| Architecture area | Markdown files | LOC |
|---|---:|---:|
| `minecraft-target` | 36 | 2,568 |
| `loader-api` | 4 | 269 |
| `security` | 3 | 207 |
| `steelhook` | 2 | 192 |
| `foundation` | 3 | 109 |
| `templates` | 2 | 52 |
| `[architecture root]` | 2 | 42 |

## Generated and excluded content note

These stats are intended to describe source-controlled project shape. Build products, caches, downloaded dependencies, generated runtime artifacts, local scratch files, IDE metadata, and OS-specific clutter should not be included when refreshing this file.

The Gradle wrapper JAR is intentionally retained in file counts because it is part of the repository shape, but it is excluded from LOC because it is binary.

## Historical snapshots

| Date | Total LOC | Files | Main LOC | Test LOC | Markdown LOC | Notes |
|---|---:|---:|---:|---:|---:|---|
| 2026-05-15 | 92,712 | 730 | 47,986 | 36,715 | 6,598 | Initial project stats snapshot. |

## Refresh guidance

Refresh this file only when a stats snapshot is intentionally useful. Keep the file compact, stable, and descriptive. Do not treat LOC growth as a goal by itself.
