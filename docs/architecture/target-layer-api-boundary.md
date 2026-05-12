# Target Layer API Boundary

This pass defines the next Minecraft-facing boundary before any injection hook implementation lands.

Target-1 now adds artifact interpretation before hook contracts. That pass remains analysis-only and stays inside `target-minecraft`.

Target-2 now adds explicit hook point contract validation on top of Target-1 interpretation. That pass also remains analysis-only and stays inside `target-minecraft`.

Target-3 now selects the first internal known-symbol catalog and validates it non-invasively against Target-1 interpretation. That pass also remains analysis-only and stays inside `target-minecraft`.

Target-5 now adds an internal hook placement analysis scaffold on top of Target-3 plus the existing runtime and execution planning path. That pass reads one selected method `Code` attribute as opaque bytecode, fingerprints that method body, and writes one deterministic method-entry placement candidate without modifying Minecraft.

## Injection Hook Subsystem

The Injection Hook Subsystem is the low-level subsystem inside the Minecraft Target Layer.

It communicates with Minecraft internals through hook points, mapped symbols, classloading boundaries, and deterministic diagnostics.

It is not a standalone public API and it is not the ergonomic modding surface.

The first concrete internal contract in that subsystem is now the Target-2 hook point contract model under `com.spindle.core.minecraft.hook`.

That model progresses through:

1. Target-1 reads class-file structure from planned Minecraft runtime jars.
2. Target-2 defines validation-only hook contracts.
3. Target-3 selects the first internal known-symbol catalog and validates it non-invasively.
4. Target-4 installs one minimal launch-boundary wrapper around `net.minecraft.server.Main.main(String[])`.
5. Target-5 reads one selected method `Code` attribute as opaque bytes and plans one method-entry placement candidate.
6. Future passes may expand placement analysis and broader hook families.

Target-5 still does not decode instructions, inspect callsites, modify bytecode, install hooks, expose a public API, add gameplay hooks, use Mixin or Java agents, or imply Java mod execution is sandboxed.

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

Target-2 and Target-3 remain analysis-only scaffolding inside that boundary. Target-4 adds one internal launch-boundary installation proof. Target-5 adds one internal method-entry placement analysis scaffold without crossing into bytecode injection, public hook APIs, gameplay-facing modding surfaces, or sandbox claims.
