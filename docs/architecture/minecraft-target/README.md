# Minecraft Target Layer Architecture

This folder records Minecraft Target Layer concept grounding, Target-* pass history, and internal boundaries.

Current status: Target-1 through Target-10 complete the narrow SteelHook 0.1 fake-server hook spine. Target-11 through Target-22 ground server lifecycle, command registration, resource/reload, and registry bootstrap/content registration concept families as analysis-only or synthesis passes. Target-23 through Target-27 complete SteelHook 0.2 in the narrow capability-ladder sense. Target-28 adds the bounded framed-method foundation for the existing method-entry static-dispatch primitive, Target-29 adds bounded offline `METHOD_EXIT_STATIC_DISPATCH` proof before supported normal return opcodes, Target-30 proves both approved SteelHook 0.3 primitives through isolated gated runtime class-definition sessions, Target-31 completes SteelHook 0.3 through deterministic handoff verification, and Target-32 begins SteelHook 0.4 as an analysis-only primitive-boundary pass.

SteelHook 0.1 proves the first bounded hook spine against controlled fixtures. It establishes Minecraft artifact interpretation, hook contract validation, known-symbol selection, hook placement analysis, instruction-aware bytecode modeling, dry-run patch planning, fixture-only transformation, bootstrap class transformation plumbing, and a hardening caboose. It does not expose a public hook API or support broad bytecode mutation.

SteelHook 0.2 proves one approved method-entry static-dispatch primitive through a gated runtime classloader definition path. Its completed capability is intentionally narrow: the approved target is `net.minecraft.server.Main.main([Ljava/lang/String;)V`, the dispatcher is `SteelHookDispatcher.beforeMinecraftServerMain()V`, and the runtime proof defines the transformed target class without initializing it or invoking Minecraft main.

SteelHook 0.2 does not mean arbitrary bytecode editing, Mixin replacement, public SteelHook APIs, Java mod execution sandboxing, StackMapTable rewriting, method-exit hooks, cancellable hooks, callsite redirects, return-value interception, field hooks, constructor hooks, multi-hook composition, registry/command/resource/lifecycle implementation, server launch, hook installation, or dispatcher observation.

SteelHook 0.3 is now complete, but the completed boundary remains deliberately narrow. Target-28 supports bounded first-frame StackMapTable shifting for method-entry insertion at offset `0`, Target-29 supports offline-only method-exit static dispatch before supported normal return opcodes in controlled unframed fixtures, Target-30 proves isolated gated runtime class definition for those two primitives without composing hooks, invoking Minecraft main, launching Minecraft, installing hooks, executing dispatchers, exposing public APIs, or claiming sandboxing, and Target-31 verifies that evidence chain and writes `minecraft-steelhook-0-3-report.json`. SteelHook 0.3 still does not add full frame recomputation, exceptional-exit observation, return-value interception, hook installation, public API exposure, server launch, or sandbox claims.

SteelHook 0.4 is now complete only within the Target-36 verifier boundary. Target-32 records only the approved planned internal primitive families `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`, Target-33 proves `RETURN_VALUE_INTERCEPT` offline only against one controlled primitive-return fixture and one controlled reference-return fixture in both observation-only and replacement modes, Target-34 proves `INVOKE_REDIRECT` plus `INVOKE_WRAP` offline only against one controlled `invokestatic` callsite shape with strict owner, name, descriptor, and opcode matching, and Target-35 proves isolated gated runtime class definition through `Class.forName(binaryName, false, runtimeClassLoader)` without initialization or execution beyond class definition. Target-36 then verifies the persisted Target-32 through Target-35 evidence chain, rejects stale side-effect reports, raw byte payloads, unsupported primitive leakage, and reports that imply execution beyond class definition, and writes `minecraft-steelhook-0-4-report.json` with `steelhook-0-4-complete` only when the completed internal primitive set remains exactly `RETURN_VALUE_INTERCEPT`, `INVOKE_REDIRECT`, and `INVOKE_WRAP`. These are still internal primitives, not public APIs. Java mod execution is not sandboxed.

The Target Layer concept arcs after SteelHook 0.1 remain analysis/synthesis grounding work. Server lifecycle, commands, resources/reload, and registry bootstrap/content registration identify Minecraft-facing concepts, symbols, boundaries, and next-direction decisions. They do not yet turn those concepts into a public modding API.

Future Target Layer, SteelHook, and Modding API planning should inspect `minecraft-target-concept-roadmap.md` before adding new Minecraft-facing concept families or names.

## Concept folders

- `steelhook-0.1/`: Target-1 through Target-10 and the SteelHook 0.1 capability boundary.
- `server-lifecycle/`: Target-11 and Target-12 lifecycle grounding.
- `commands/`: Target-13 through Target-15 command grounding.
- `resources-reload/`: Target-16 through Target-20 resource/reload grounding and caboose decision.
- `registry-bootstrap/`: Target-21 and Target-22 registry bootstrap/content registration grounding.
- `steelhook-0.2/`: Target-23 through Target-27 and the completed bounded SteelHook 0.2 primitive path.
- `steelhook-0.3/`: Target-28 onward and the bounded framed-method expansion path.
- `steelhook-0.4/`: Target-32 onward and the bounded internal primitive-family boundary for SteelHook 0.4.

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

## SteelHook 0.2 completion boundary

SteelHook 0.2 is complete only within the documented Target-27 handoff boundary. The completed proof verifies the Target-7 through Target-26 report chain, confirms the approved primitive and patch-plan descriptors, checks the offline method-entry transformer result, checks the gated runtime transformation result, and rejects stale or unsafe side-effect reports.

The handoff state for SteelHook 0.2 is `steelhook-0-2-complete` when the completion verifier reports `status: "passed"` and `completionReady: true`.

This completion boundary preserves the existing runtime and security semantics. Java mod execution is not sandboxed. SteelHook 0.2 does not claim sandboxing and does not broaden runtime behavior beyond the single gated class-definition proof.
