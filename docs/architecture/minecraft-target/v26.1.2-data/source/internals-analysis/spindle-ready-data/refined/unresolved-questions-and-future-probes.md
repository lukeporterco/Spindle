# Unresolved Questions And Future Probes

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted

These questions must remain visible so future implementation work does not inherit hidden assumptions from dedicated-server-only evidence.

## Required Future Probes Or Decisions

- Integrated server startup/lifecycle parity.
- Reload `this.resources` assignment and post-swap update ordering.
- Exact command constructor-tail insertion proof.
- Whether command registration replay should happen during reload.
- Subsystem-specific shutdown ordering only if future APIs need it.
- Networking phase event inclusion decision.
- Registry mutation/contribution remains not-first-wave unless a future design explicitly reopens it.

## Standing Boundaries

- Dedicated server only runtime evidence is available in this pass.
- Integrated server parity is not confirmed.
- Reload/datapack contribution is not ready until async/post-swap proof exists.
- Command registration is not ready until constructor-tail insertion is proven.
- Registries remain lookup-only.
- Java mod execution is not sandboxed.
