# Confidence Matrix

## Purpose

Track source confidence and what remains before implementation planning.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

| Concept | Static source confidence | Runtime confirmation | Implementation readiness | Main follow-up |
|---|---|---|---|---|
| Server lifecycle | high | missing | partial | Probe dedicated/integrated ready and shutdown order |
| Commands | high ownership, medium insertion | missing | partial | Prove external registration timing before function compile |
| Registries | high lookup/layers, medium contribution | missing | lookup-ready only | Research mutation/contribution separately |
| Resources and datapacks | high reload structure, medium safety | missing | event-only partial | Probe `/reload` post-swap completion |
| World and level | high accessors, medium readiness | missing | lookup-ready after lifecycle | Decide snapshot versus live view |
| Ticking | high | not required for docs | ready for first pass | Decide event names/order |
| Networking | high phase structure, medium public scope | missing | documentation/phase-only | Decide whether first wave includes networking |
| Data generation and assets | medium | not applicable | documentation-only | Inspect Fabric datagen wiring if needed |

## Confidence Rules Applied

- High means exact source paths and line-backed behavior exist.
- Medium means static source is strong but lifecycle safety, side split, or composition needs runtime/design proof.
- No runtime probe was run in this pass.
