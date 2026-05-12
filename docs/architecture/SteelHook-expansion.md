SteelHook is the bottleneck for Spindle’s Minecraft modding capability. Anything the future Target Layer or Modding API can do to Minecraft must eventually bottom out into one of three things:

1. A Minecraft symbol SteelHook can identify.
2. A bytecode or class-structure change SteelHook can safely apply.
3. A runtime dispatcher path SteelHook can invoke predictably.

So the real question is not “Should SteelHook be powerful?” It should. The question is **where that power is exposed** and **how much arbitrary control is allowed at each layer**.

SteelHook should eventually be capable of nearly any Minecraft runtime change, but it should not become a raw public “do anything to bytecode” API for normal developers.

The intended layer split should be:

SteelHook is the machine layer. It knows class files, bytecode, constant pools, method bodies, offsets, frames, dispatcher calls, transformed class bytes, and classloading. It should be powerful, sharp, internal, deterministic, and heavily validated.

The Minecraft Target Layer is the assembly layer. It translates SteelHook machinery into named Minecraft-specific concepts like “server main entrypoint,” “server tick method,” “registry bootstrap,” “command registration,” “packet receive path,” “world load path,” or “entity update path.” It should still be low-abstraction, but human-readable.

The future Modding API is the programming-language layer. It gives developers ergonomic things like lifecycle callbacks, events, registries, service bindings, config hooks, networking helpers, and eventually possibly simulation hooks.

SteelHook should be capable enough to support basically any mod feature, but that does not mean every mod gets arbitrary SteelHook access. Most mods should interact with the Modding API. Advanced/internal Spindle target code should interact with the Target Layer. SteelHook itself should remain the engine.

After Target-10, SteelHook will not yet be “complete” in the sense of supporting any possible mod feature. Target-10 will complete **SteelHook 0.1**, which proves the pipeline:

`known contract -> placement -> instruction analysis -> patch plan -> bytecode transform -> bootstrap classloading -> dispatcher invocation -> deterministic reports`

That is huge architecturally, but still narrow mechanically. It proves the spine works. It does not yet mean “any Minecraft runtime change is now possible.”

SteelHook 0.1 will be capable of one controlled kind of change:

A validated method-entry static dispatcher insertion into a known target method, proven through fake-server bootstrap transformation, with deterministic reports and hardening around failure modes.

That is enough to prove Spindle owns a hook lifecycle without Mixin, Java agents, ASM, or public arbitrary injection. It is not enough for broad modding yet.

For SteelHook to become the bottleneck that can support “basically any” Minecraft feature, it needs to grow from one patch primitive into a set of carefully validated primitives.

The essential future SteelHook capabilities are:

| Capability area         | What it unlocks                                                    |
| ----------------------- | ------------------------------------------------------------------ |
| Method entry hooks      | lifecycle start, tick start, command/bootstrap entrypoints         |
| Method exit hooks       | after events, result observation, cleanup paths                    |
| Return-value hooks      | cancellable events, result replacement, behavior override          |
| Callsite hooks          | redirecting or wrapping specific Minecraft calls                   |
| Field read/write hooks  | state observation, state replacement, compatibility shims          |
| Constructor hooks       | object creation observation or initialization                      |
| Class initializer hooks | static bootstrap and registry timing hooks                         |
| Method body replacement | deep behavior overrides where insertion is insufficient            |
| Method addition         | helper bridges, synthetic adapters, generated access paths         |
| Field addition          | internal state attachment, though this needs care                  |
| Access changes          | controlled access to otherwise private/protected internals         |
| Interface injection     | making Minecraft objects satisfy internal extension contracts      |
| Constant replacement    | tuning hardcoded values or feature switches                        |
| Multi-hook composition  | several mods/hooks touching the same target deterministically      |
| Conflict detection      | refusing or ordering incompatible changes                          |
| Version catalogs        | mapping stable Spindle concepts onto obfuscated Minecraft versions |
| StackMapTable rewriting | required for real broad runtime transformation                     |
| Dispatcher conventions  | cancellation, priorities, return override, error handling          |
| Reportability           | every change traceable before runtime execution                    |

Those are the things that make SteelHook broadly capable.

But the important part is that SteelHook should not expose those as “here is arbitrary bytecode mutation, have fun.” It should expose them internally as **validated patch modes**. For example:

`METHOD_ENTRY_STATIC_DISPATCH`

`METHOD_EXIT_STATIC_DISPATCH`

`RETURN_VALUE_INTERCEPT`

`INVOKE_REDIRECT`

`INVOKE_WRAP`

`FIELD_GET_REDIRECT`

`FIELD_SET_REDIRECT`

`CONSTANT_REPLACE`

`ACCESS_WIDEN`

`SYNTHETIC_METHOD_ADD`

`INTERFACE_ATTACH`

That lets SteelHook become extremely powerful without becoming chaotic.

This also helps with capability reconciliation. “Basically any mod feature” usually reduces to one or more of these underlying needs:

A mod wants to know when something happens. SteelHook needs observation hooks.

A mod wants to stop something from happening. SteelHook needs cancellable hooks or call redirects.

A mod wants to change a result. SteelHook needs return-value interception.

A mod wants to replace behavior. SteelHook needs method replacement or redirect primitives.

A mod wants to add state. SteelHook needs field addition, side tables, or component attachment.

A mod wants to expose private game internals. SteelHook needs access changes or generated bridge methods.

A mod wants to alter game initialization. SteelHook needs class-init, bootstrap, and registry timing hooks.

A mod wants deep performance changes. SteelHook needs method replacement, redirects, and eventually simulation-aware hooks.

So the ambition is valid. The caution is only about **how directly** that power is given to mod authors.

The rule I would use is:

**SteelHook should be powerful enough to implement almost anything, but structured enough that Spindle can explain, validate, order, report, and refuse unsafe changes.**

That means SteelHook should be capable, not permissive.

The Minecraft Target Layer then decides which SteelHook operations are meaningful for Minecraft. For example, SteelHook might support “insert method-entry dispatcher,” but the Target Layer turns that into “server lifecycle before main,” “server tick before,” or “registry bootstrap before.” SteelHook might support “invoke redirect,” but the Target Layer turns that into a named low-level escape hatch like “redirect this known chunk-load callsite.”

The Modding API later turns those into normal developer concepts like:

`onServerStarting`

`onServerTick`

`registerCommand`

`registerBlock`

`listenForPacket`

`attachComponent`

`replaceLootTable`

`scheduleWorldTask`

So your instinct is right: **SteelHook is the limiting substrate.** If SteelHook cannot express a runtime change, the Target Layer cannot expose it cleanly, and the Modding API cannot offer it honestly.

Where I would refine the goal is this:

SteelHook should not initially try to support “any arbitrary bytecode change.”

SteelHook should eventually support “enough validated patch primitives that any meaningful Minecraft modding feature can be represented as one or more deterministic operations.”

That is a better architecture because it gives you the same power ceiling, but with better safety, diagnostics, composition, and versioning.

There are also a few hard truths.

First, truly arbitrary changes are impossible to make universally safe. Some mods will always want to replace large systems, mutate assumptions, break invariants, or conflict with each other. SteelHook can make those changes possible, but it cannot make all combinations valid.

Second, “basically any feature” requires StackMapTable support eventually. Target-8 and Target-9 deliberately reject StackMapTable. That is correct for now, but not sufficient forever. Real Minecraft runtime transformation will eventually need either frame preservation, frame adjustment, or frame recomputation.

Third, versioning will become as important as bytecode capability. Minecraft is obfuscated and version-sensitive. SteelHook can be powerful, but the Target Layer needs catalogs that say exactly which Minecraft version, side, class, method, descriptor, bytecode fingerprint, and placement shape are supported.

Fourth, composition is mandatory. If two mods want the same method-entry hook, that is easy. If two mods want to redirect the same callsite, replace the same return value, or mutate the same constant, SteelHook needs deterministic conflict semantics.

Fifth, SteelHook is not a sandbox. It can restrict and validate transformation shape, but Java mod execution remains Java execution unless a separate runtime isolation model is built. The docs should continue saying that plainly.

My model for SteelHook maturity would be:

SteelHook 0.1, through Target-10:
Proves the full custom hook pipeline on one controlled fake-server transformation path. It is a spine, not broad capability.

SteelHook 0.2:
Supports real Minecraft runtime transformation for one or two carefully chosen no-StackMap or manageable targets, with stronger verifier checks.

SteelHook 0.3:
Adds StackMapTable handling and more insertion modes, probably method exit and cancellable entry hooks.

SteelHook 0.4:
Adds callsite redirection/wrapping and return-value interception.

SteelHook 0.5:
Adds multi-hook composition, priorities, conflict reports, and richer dispatcher conventions.

SteelHook 1.0:
Has enough validated primitives, version catalogs, dispatcher semantics, and conflict handling that the Minecraft Target Layer can expose a serious low-level API, and the Modding API can build normal developer-facing features on top.

So, reconciled version:

Your goal is correct. SteelHook should be the engine that makes Spindle capable of deep Minecraft runtime changes. My caution is that it should become powerful through **small validated primitives**, not through a public arbitrary injection free-for-all.

In one sentence:

**SteelHook should be a general-purpose internal Minecraft transformation engine, but not a general-purpose public bytecode mutation API.**

That gives Spindle the capability ceiling you want while still preserving the architecture that makes it maintainable.
