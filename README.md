# Enchantment Storage

![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.4-purple)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-green)
![Release](https://img.shields.io/badge/Release-v1.0.0-informational)
![Stable](https://img.shields.io/badge/Channel-Stable-brightgreen)

> minecraft skin placeholder

## Table of Contents

- [Player Usage](#player-usage)
  - [Installation](#installation)
  - [Crafting Recipes](#crafting-recipes)
  - [GUI Basics](#gui-basics)
  - [Store & Extract Flow](#store--extract-flow)
  - [Experience & Lapis Cost](#experience--lapis-cost)
- [Hopper Behavior](#hopper-behavior)
- [Gallery](#gallery)
- [Debug Commands](#debug-commands)
- [Developer Notes](#developer-notes)
  - [Gradle](#gradle)
  - [JVM Args](#jvm-args)
  - [Spotless](#spotless)
  - [Unit Testing](#unit-testing)
  - [Game Testing](#game-testing)
  - [Enchantment Registry](#enchantment-registry)
- [Clean Code Guidelines](#clean-code-guidelines)
- [Source Regions](#source-regions)
- [Unit Test Regions](#unit-test-regions)
- [GitHub Repository](#github-repository)
- [PRs & Issues](#prs--issues)
- [Donations](#donations)
- [License](#license)

## Player Usage

### Installation

- Place the downloaded `.jar` file into your Minecraft `mods/` folder.
- Use the same Minecraft version as this mod (`1.21.10`) and ensure Fabric Loader + Fabric API are installed.

### Crafting Recipes

There are two valid shaped recipes to craft an `Enchantment Extractor`:

Requires:
  * Obsidian x4
  * Lapis Lazuli x2
  * Ender Eye x1
  * Dragon's Breath or End Crystal x2

> crafting recipes placeholder

### GUI Basics

- Slot layout:
  - Top input slot can only hold lapis lazuli
  - Bottom input slot can only hold books, enchanted books, or enchanted items
  - Right slot is output only
- Two action buttons:
  - `Store`: pulls enchantments from bottom input into internal storage
  - `Extract`: applies selected enchantment onto a book (cannot extract onto other items)

> GUI placeholder

### Store & Extract Flow

1. Place lapis in top slot.
2. Place an enchanted item or enchanted book in input slot, then press `Store`.
3. Select a stored enchantment level from the list.
4. Place a book in bottom input, then press `Extract`.
5. Take result from output slot.

### Experience & Lapis Cost

- `Store` base XP cost = total enchantment levels on the input item/book
- `Extract` base XP cost = selected enchantment rank (`I=1`, `II=2`, `III=3`, etc.)
- Lapis gives a direct discount of 1 experience level per lapis
- One lapis is consumed per level discounted

> store and extract example placeholders

## Hopper Behavior

- Top face (`UP`): for lapis slot only.
- Side faces (`N/E/S/W`): for item input slot only.
- Bottom face (`DOWN`): for output slot extraction only.

> hopper setup placeholder

## Gallery

> gallery placeholder

## Debug Commands

Debug commands are dev-only and require permission level `2`. Root command: `/es`

| Description                          | Command                                       |
|--------------------------------------|-----------------------------------------------|
| Seed internal enchantment storage    | `/es seed [rows] [quantity]`                  |
| Print stored enchantments and total  | `/es list`                                    |
| Clear all stored enchantment data    | `/es clear`                                   |
| Print input/output/lapis slot states | `/es inspect`                                 |
| Set lapis slot count (`0..64`)       | `/es fillLapis <count>`                       |
| Add enchantment to storage           | `/es store <enchantment> <rank> <quantity>`   |
| Remove enchantment from storage      | `/es extract <enchantment> <rank> <quantity>` |


## Developer Notes

### Gradle

Use`.\gradlew <task>` to run Gradle wrapper tasks.

| Task            | Description                                             |
|-----------------|---------------------------------------------------------|
| `runClient`     | Launches the Minecraft client with the mod in dev mode. |
| `build`         | Compiles, tests, and builds the mod jar.                |
| `test`          | Runs JUnit unit tests.                                  |
| `runGameTest`   | Runs Fabric/Minecraft game tests.                       |
| `runDatagen`    | Runs data generation for assets/data outputs.           |
| `spotlessCheck` | Verifies formatting/lint rules configured by Spotless.  |
| `spotlessApply` | Applies Spotless formatting fixes automatically.        |
| `clean`         | Deletes the build directory for a fresh rebuild.        |

> Use `--refresh-dependencies` to force Gradle to do a full rebuild.

### JVM Args

- Global Gradle JVM args from `gradle.properties`
- Java version requirement: `Java 21`
- Client run profile JVM args from `build.gradle`:
  - `-Xms1G`
  - `-Xmx2G`
  - `-XX:+UseG1GC`
  - `-XX:MaxGCPauseMillis=100`

### Spotless

```powershell
.\gradlew spotlessApply
.\gradlew spotlessCheck
```

### Unit Testing

```powershell
.\gradlew test
```

### Game Testing

```powershell
.\gradlew runGameTest
```

### Enchantment Registry

- Implemented in `src/main/java/com/lexxeous/enchantment_storage/blockentity/EnchantmentExtractorBlockEntity.java:140`.
- Purpose: resolve the enchantment registry from world registry manager.
- Returns `null` when world is unavailable, so callers must handle nullable results.

## Clean Code Guidelines

1. Keep nullability contracts explicit and consistent.
2. Replace magic numbers with named constants.
3. Keep logs structured and low-noise.
4. Keep client-only code out of common/server paths.
5. Keep NBT serialization version-aware and validated.
6. Use one clear registration/bootstrap path.
7. Avoid avoidable allocations in hot UI/render loops.
8. Keep utility classes focused by domain.
9. Move deterministic logic into pure testable helpers.
10. Use consistent naming and verb conventions.
11. Add compatibility guards where APIs/mappings shift.
12. Enforce formatting and static checks in automation.

## Source Regions

| Region                          | Scope                                                        |
|---------------------------------|--------------------------------------------------------------|
| `Constants`                     | Static/final values shared by the class.                     |
| `Class Variables`               | Instance fields representing class state.                    |
| `Constructors`                  | Object construction and initialization wiring.               |
| `Registration & Initialization` | Startup registration and one-time bootstrapping flows.       |
| `Getters & Setters`             | Accessors/mutators for controlled state access.              |
| `UI`                            | UI-facing behavior, layout/state helpers, and interactions.  |
| `Overrides`                     | Implementations of superclass/interface contracts.           |
| `Public`                        | Public API methods intended for external callers.            |
| `Protected`                     | Extension points intended for subclasses/packages.           |
| `Private`                       | Internal implementation details hidden from callers.         |
| `Helpers`                       | Reusable internal utility methods supporting core logic.     |
| `Validation`                    | Input/state guards and invariant checks.                     |
| `Serialization`                 | Read/write logic for persisted/transferred data.             |
| `Debug`                         | Dev/test-only instrumentation and debug utilities.           |
| `Logging`                       | Structured logging helpers and related routines.             |

## Unit Test Regions

| Region        | Scope                                     |
|---------------|-------------------------------------------|
| `Vanilla`     | Vanilla enchantment/runtime assumptions   |
| `Modded`      | Modded compatibility behavior             |
| `Smoke`       | Fast pass/fail sanity tests               |
| `Confidence`  | Core logic utilities                      |
| `Regression`  | Bugfix lock-in tests                      |
| `Stability`   | Top-level behavior validation             |
| `Integration` | Screen/block entity/game test integration |

## GitHub Repository

- https://github.com/Lexxeous/enchantment_storage

## PRs & Issues

- Pull Requests: https://github.com/Lexxeous/enchantment_storage/pulls
- Issues: https://github.com/Lexxeous/enchantment_storage/issues

## Donations

- Venmo: [@Lexxeous](https://venmo.com/Lexxeous)
- Cash App: [$Lexxeous](https://cash.app/$Lexxeous)

## License

Click here to view the [license](LICENSE).