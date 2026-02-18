# ThinkGearReaderFX

## Overview

Legacy JavaFX operator UI that consumes brainwave stream events and renders realtime charts/spectrum views.

## Scope in BrainWaves

- In-scope as compatibility visualization client
- Main consumed topic: `think_gear_reader`
- Main producer dependency: `ThinkGearReader`

## Tech Stack

- Java 11
- JavaFX 14
- Spring Boot 2.2.x
- Akka Kafka consumer
- Gradle

## Build

```bash
./gradlew clean build
```

## Run

```bash
./gradlew bootRun
```

## Key Configuration / Integration

- Config file: `src/main/resources/application.yml`
- Important keys:
  - `spring.kafka.bootstrap-servers`
  - `spring.kafka.consumer.group-id`
- Cross-repo dependency: `:commons` via `settings.gradle` (legacy path expectation: `../Commons`)

## Status / Notes

- Keep current flow active during migration.
- Planned enhancement: toggle old topic (`think_gear_reader`) and new canonical topics (`brainwaves.cleaned.v1`, `brainwaves.events.v1`).