# Minecraft Target Layer Architecture

This folder records Minecraft Target Layer concept grounding, Target-* pass history, and internal boundaries.

The imported Minecraft `26.1.2` research data now lives under `v26.1.2-data/`. Its `refined/` folder is the canonical version-specific planning input for future Minecraft Target Layer and SteelHook passes, while `source/internals-analysis/` preserves the full evidence archive. The import is documentation and data only; it does not add APIs, hooks, runtime transformations, compatibility behavior, or sandboxing.

Current status: Target-1 through Target-10 complete the narrow SteelHook 0.1 fake-server hook spine. Target-11 through Target-22 ground server lifecycle, command registration, resource/reload, and registry bootstrap/content registration concept families as analysis-only or synthesis passes. Target-23 through Target-27 complete SteelHook 0.2 in the narrow capability-ladder sense. Target-28 through Target-31 complete SteelHook 0.3 as a bounded framed-method expansion path. Target-32 through Target-36 complete SteelHook 0.4 inside a strict verifier-backed boundary covering only the approved internal primitive families `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`.

SteelHook 0.1 proves the first bounded hook spine against controlled fixtures. It establishes Minecraft artifact interpretation, hook contract validation, known-symbol selection, hook placement analysis, instruction-aware bytecode modeling, dry-run patch planning, fixture-only transformation, bootstrap class transformation plumbing, and a hardening caboose. It does not expose a public hook API or support broad bytecode mutation.

SteelHook 0.2 proves one approved method-entry static-dispatch primitive through a gated runtime classloader definition path. Its completed capability is intentionally narrow: the approved target is `net.minecraft.server.Main.main([Ljava/lang/String;)V`, the dispatcher is `SteelHookDispatcher.beforeMinecraftServerMain()V`, and the runtime proof defines the transformed target class without initializing it or invoking Minecraft main.

SteelHook 0.2 does not mean arbitrary bytecode editing, Mixin replacement, public SteelHook APIs, Java mod execution sandboxing, StackMapTable rewriting, method-exit hooks, cancellable hooks, callsite redirects, return-value interception, field hooks, constructor hooks, multi-hook composition, registry/command/resource/lifecycle implementation, server launch, hook installation, or dispatcher observation.

SteelHook 0.3 is now complete, but the completed boundary remains deliberately narrow. Target-28 supports bounded first-frame StackMapTable shifting for method-entry insertion at offset `0`, Target-29 supports offline-only method-exit static dispatch before supported normal return opcodes in controlled unframed fixtures, Target-30 proves isolated gated runtime class definition for those two primitives without composing hooks, invoking Minecraft main, launching Minecraft, installing hooks, executing dispatchers, exposing public APIs, or claiming sandboxing, and Target-31 verifies that evidence chain and writes `minecraft-steelhook-0-3-report.json`. SteelHook 0.3 still does not add full frame recomputation, exceptional-exit observation, return-value interception, hook installation, public API exposure, server launch, or sandbox claims.

SteelHook 0.4 is now complete only within the Target-36 verifier boundary. Target-32 records only the approved planned internal primitive families `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`, Target-33 proves `RETURN_VALUE_INTERCEPT` offline only against one controlled primitive-return fixture and one controlled reference-return fixture in both observation-only and replacement modes, Target-34 proves `INVOKE_REDIRECT` plus `INVOKE_WRAP` offline only against one controlled `invokestatic` callsite shape with strict owner, name, descriptor, and opcode matching, and Target-35 proves isolated gated runtime class definition through `Class.forName(binaryName, false, runtimeClassLoader)` without initialization or execution beyond class definition. Target-36 then verifies the persisted Target-32 through Target-35 evidence chain, rejects stale side-effect reports, raw byte payloads, unsupported primitive leakage, and reports that imply execution beyond class definition, and writes `minecraft-steelhook-0-4-report.json` with `steelhook-0-4-complete` only when the completed internal primitive set remains exactly `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`. These are still internal primitives, not public APIs. Java mod execution is not sandboxed.

The Target Layer concept arcs after SteelHook 0.1 remain analysis/synthesis grounding work. Server lifecycle, commands, resources/reload, and registry bootstrap/content registration identify Minecraft-facing concepts, symbols, boundaries, and next-direction decisions. They do not yet turn those concepts into a public modding API.

Future Target Layer, SteelHook, and Modding API planning should inspect `minecraft-target-concept-roadmap.md` before adding new Minecraft-facing concept families or names.

## Concept folders

- `v26.1.2-data/`: imported Minecraft `26.1.2` research archive and refined Spindle-facing planning data.
- `steelhook-0.1/`: Target-1 through Target-10 and the SteelHook 0.1 capability boundary.
- `server-lifecycle/`: Target-11 and Target-12 lifecycle grounding.
- `commands/`: Target-13 through Target-15 command grounding.
- `resources-reload/`: Target-16 through Target-20 resource/reload grounding and caboose decision.
- `registry-bootstrap/`: Target-21 and Target-22 registry bootstrap/content registration grounding and handoff.
- `steelhook-0.2/`: Target-23 through Target-27 and the completed bounded SteelHook 0.2 primitive path.
- `steelhook-0.3/`: Target-28 through Target-31 and the bounded framed-method expansion path.
- `steelhook-0.4/`: Target-32 through Target-36 and the bounded internal primitive-family boundary for SteelHook 0.4.

## Target pass index

### SteelHook 0.1

- Target-1: Minecraft artifact interpretation.
- Target-2: Hook point contract model.
- Target-3: Known-symbol hook validation.
- Target-4: Minimal hook installation proof.
- Target-5: Hook placement analysis scaffold.
- Target-6: Instruction-aware bytecode model.
- Target-7: Injection patch planning dry-run.
- Target-8: Fixture-only bytecode transformation.
- Target-9: Bootstrap class transformation path.
- Target-10: SteelHook 0.1 hardening caboose.

### Server lifecycle

- Target-11: Server lifecycle binding analysis.
- Target-12: Server lifecycle dispatch plan.

### Commands

- Target-13: Command registration concept analysis.
- Target-14: Command dispatcher symbol analysis.
- Target-15: Command dispatcher binding analysis.

### Resources and reload

- Target-16: Resource reload boundary analysis.
- Target-17: Resource reload symbol analysis.
- Target-18: Resource reload binding analysis.
- Target-19: Resource visibility and generation separation.
- Target-20: Resource reload arc caboose.

### Registry bootstrap and content registration

- Target-21: Registry bootstrap/content registration analysis.
- Target-22: Registry arc hardening synthesis.

### SteelHook 0.2

- Target-23: SteelHook 0.2 primitive boundary.
- Target-24: SteelHook 0.2 contract generalization.
- Target-25: SteelHook 0.2 method-entry transformer.
- Target-26: SteelHook 0.2 gated runtime transformation.
- Target-27: SteelHook 0.2 completion.

### SteelHook 0.3

- Target-28: Framed method foundation.
- Target-29: Method-exit static dispatch.
- Target-30: Generalized transformer gated runtime proof.
- Target-31: SteelHook 0.3 completion.

### SteelHook 0.4

- Target-32: SteelHook 0.4 primitive boundary.
- Target-33: Return-value intercept offline proof.
- Target-34: Invoke redirect and wrap offline proof.
- Target-35: Gated runtime proof.
- Target-36: SteelHook 0.4 completion.

## Latest handoff

The latest completed handoff is SteelHook 0.4 at Target-36. That verifier accepts the persisted Target-32 through Target-35 evidence chain only when the approved primitive set remains exactly `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`, no unsupported primitive evidence leaks into the reports, no raw byte payloads are serialized, and no report implies execution beyond isolated class definition.

That handoff is intentionally still internal-only. It does not install hooks into real Minecraft runtime execution, expose a public SteelHook API, expose a public Minecraft modding API, launch Minecraft through transformed production classes, or change the project's non-sandboxed Java execution posture.

The next handoff, when work resumes, should build from this completed SteelHook 0.4 boundary or from one of the grounded Minecraft concept arcs without broadening capability claims retroactively.
