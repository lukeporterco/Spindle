# SteelHook 0.1 Capability Boundary

SteelHook 0.1 is complete only as a narrow internal proof.

It proves one bounded chain:

```text
known contract
-> method-entry placement
-> instruction-aware bytecode analysis
-> dry-run patch planning
-> fixture transform primitive
-> fake-server bootstrap transformation
-> dispatcher invocation
-> completion verification
```

## Layer Split

```text
SteelHook 0.1
  Narrow fake-server method-entry transform proof for one exact target:
  net/minecraft/server/Main.main([Ljava/lang/String;)V

Future SteelHook
  Broader internal transformation engine work such as more hook families,
  runtime-safe rewriting, and stronger verifier/planner coverage.

Minecraft Target Layer
  Low-abstraction Minecraft-specific concepts, reports, planning, and runtime
  ownership built over internal SteelHook machinery.

Future Modding API
  Ergonomic developer-facing APIs for events, registries, commands,
  networking, resources, client behavior, and gameplay surfaces.
```

## Included In SteelHook 0.1

- internal known-symbol validation for one Minecraft `26.1.2` server entrypoint contract
- deterministic method-entry placement planning
- deterministic instruction-aware bytecode analysis
- deterministic dry-run patch planning
- fixture-only transformed-class proof
- fake-server bootstrap classloader application of that proof
- dispatcher invocation observation
- deterministic completion verification and reporting

## Explicitly Excluded From SteelHook 0.1

- real Minecraft runtime artifact transformation
- arbitrary classpath transformation
- general transformer framework claims
- StackMapTable rewriting
- arbitrary hooks or multi-hook composition
- method-exit, return, callsite, field, constructor, and class-initializer hooks
- public hook APIs
- gameplay hooks
- Modding API events or lifecycle surfaces
- Mixin, Java agents, remapping, and access wideners
- Java sandbox claims for mod execution

SteelHook 0.1 should be described as an internal fake-server-only method-entry transform proof, not as a general Minecraft runtime transformation subsystem.
