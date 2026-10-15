# Imperial Dominion Bot

An autonomous Java strategy bot for the Imperial Dominion simulation game.

The bot plans long-term resource development across workers, housing, energy, food, iron, research buildings, and the final rocket science objective. It uses staged decision-making, safety checks, resource production/consumption estimates, and action scheduling to complete the simulation.

## Highlights

- Stage-based strategy from early worker growth to late-game science production
- Resource balancing for food, energy, wood, iron, workers, and building slots
- Safety logic to avoid running out of available workers
- Diagnostic logging for stage progress and blocked actions
- Completed a full run ending in `COMPLETE (SUCCESS)`

## Files

```text
ImperialBot.java
RUN_RESULT.md
```

## About the Engine

The Imperial Dominion engine and course-provided `.jar` are not included in this repository. This repo contains only my bot strategy code and a short result summary.

To run the bot, place `ImperialBot.java` in the course-provided Imperial Dominion environment and execute it with the official runner.

## Result

The included run summary shows the bot reaching the final science objective:

```text
ROCKET SCIENCE BUILT. COMPLETE CHECKPOINT.
Game ended with: COMPLETE (SUCCESS)
```

## Testing

The repository includes minimal API stubs for the proprietary course engine. They
allow CI to compile the complete bot source without redistributing the engine,
while engine-independent unit tests verify action cooldown behavior.

```bash
bash test/run-tests.sh
```

GitHub Actions runs the compile and test checks on Java 17 and Java 21. A full
simulation still requires the official course-provided runner.
