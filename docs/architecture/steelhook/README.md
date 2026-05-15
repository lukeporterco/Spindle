# SteelHook Architecture

This folder records broad SteelHook strategy and future SteelHook versioning direction.

SteelHook 0.1 Target pass documents live under `../minecraft-target/steelhook-0.1/` because they were implemented as Target-* passes. This folder should hold cross-version SteelHook strategy, future primitive planning, and documents that are not specific to a single Target pass sequence.

SteelHook 0.2 Target pass documents now begin under `../minecraft-target/steelhook-0.2/`. Target-23 selects the first approved primitive boundary as analysis-only planning input for later contract and transformer generalization work, Target-24 lifts that bounded primitive into reusable descriptors for Target-25 without transforming Minecraft classes, and Target-25 proves the reusable method-entry transformer through offline-only class-byte rewriting while still leaving runtime classloading, hook installation, and dispatch disabled.
