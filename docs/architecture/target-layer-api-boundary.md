# Target Layer API Boundary

This pass defines the next Minecraft-facing boundary before any injection hook implementation lands.

## Injection Hook Subsystem

The Injection Hook Subsystem is the low-level subsystem inside the Minecraft Target Layer.

It communicates with Minecraft internals through hook points, mapped symbols, classloading boundaries, and deterministic diagnostics.

It is not a standalone public API and it is not the ergonomic modding surface.

## Target Layer API

The Target Layer API is the readable but low-abstraction Minecraft-facing substrate.

It should expose target facts and target operations, not developer-friendly modding workflows.

Advanced developers may eventually use it as an escape hatch when they need direct target behavior.

## Modding API

The Modding API is the future ergonomic API built on top of the Target Layer API.

It should express developer intent such as events, registries, commands, resources, networking, world helpers, and gameplay abstractions.

It should not be shaped like internal hook plumbing and it should not inherit `.target` naming.

## Naming Direction

Future internal hook implementation should live under `target-minecraft`, likely below `com.spindle.core.minecraft.hook`.

Future low-level target escape hatch API, if exposed publicly, should live under a deferred Minecraft namespace such as `com.spindle.api.minecraft.target.*`.

Future ergonomic modding APIs should not use `.target` names.

## Boundary Intent

This document is a boundary-prep note only.

It names the first planned Minecraft Target Layer subsystem, the Injection Hook Subsystem, without implementing it.
