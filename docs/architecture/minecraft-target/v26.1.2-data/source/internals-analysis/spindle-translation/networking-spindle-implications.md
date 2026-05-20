# Networking Spindle Implications

## Summary

Server networking phases are source-backed, but first-wave public scope should avoid raw packets.

## First-wave Target Layer recommendation

include as event-only

## Why

Handshake/login/configuration/play/reconfiguration paths are documented by listener and protocol classes (`NET-01` through `NET-12`). Raw send is direct channel-level transport (`NET-03`).

## Boundary recommendation

If included, expose server-side phase events around `handleLoginAcknowledgement(...)`, `startConfiguration()`, `handleConfigurationFinished(...)`, and `switchToConfig()`.

## SteelHook requirement

method-entry/exit for phase events. Packet interception would require future method-around/wrap and is rejected for first wave.

## API caution

Do not expose raw `Connection.send(...)`, packet mutation, or client-side networking in first wave.

## Required follow-up before implementation

Decide whether networking is included at all in first wave or remains documentation-only.

Minecraft version: 26.1.2
Mapping namespace: Yarn named unless otherwise noted
