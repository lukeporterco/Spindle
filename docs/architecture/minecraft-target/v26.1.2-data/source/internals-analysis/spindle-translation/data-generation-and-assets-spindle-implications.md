# Data Generation And Assets Spindle Implications

## Summary

Data generation is a future-facing asset/datapack input path, not a runtime internals hook in this pass.

## First-wave Target Layer recommendation

defer

## Why

The Fabric skeleton is only a basic mod workspace (`E-001`). Vanilla `DataGenerator`, `PackOutput`, recipe, loot, and tag providers show generated JSON asset shapes (`E-002` through `E-006`). Pack ingestion flows through pack/resource manager and reload paths (`E-007` through `E-010`).

## Boundary recommendation

No runtime hook required for static generated assets that become pack contents before server reload.

## SteelHook requirement

none for offline/static generation. Runtime-generated pack insertion would be future work.

## API caution

Do not design a Spindle data generation API from this pass. Do not treat Fabric datagen conventions as vanilla runtime evidence.

## Required follow-up before implementation

Inspect Fabric datagen entrypoint wiring and decide whether Spindle wants offline generation, runtime pack contribution, or both.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
