# Spindle Modding API Naming Conventions

Spindle's Modding API should prioritize developer intent over Minecraft internals.

The public API should read like what a developer wants to do, not like the engine path required to do it. Common actions should use short, obvious calls with stable defaults, while the internal system expands those calls into complete deterministic contracts.

```java
items.add("custom_item");
blocks.add("blue_lamp");
events.tick(...);
commands.add("home", ...);
network.channel("stats");
```

## Core principle

```text
Simple declarations should produce complete deterministic contracts.
```

A short public call should still lower into a full internal contract that includes identity, namespace, target registry, defaults, generated assets, validation rules, diagnostics, and target-specific lowering behavior.

The developer-facing API should stay simple. The internal contract should stay explicit.

## Public naming goals

Spindle API names should be:

```text
intent-first
short
stable
human-readable
deterministic
hard to misuse
```

The API should avoid exposing Minecraft internals in normal mod code unless the developer is intentionally using an advanced escape hatch.

## Preferred top-level names

Use human domain words for common modding surfaces:

```text
items
blocks
events
commands
network
resources
world
client
server
config
services
```

These names describe the thing a developer is trying to work with. They should not require the developer to know the underlying registry, mapping, class, or Minecraft implementation path.

## Names to avoid in normal API calls

Avoid engine-heavy names in the common path:

```text
Registry.register
Registries.ITEM
Identifier
ResourceLocation
BootstrapContext
HolderLookup
MappedRegistry
```

These concepts may still exist internally or in advanced target-specific utilities, but they should not define the normal developer experience.

## Verb style

Use short verbs for common operations:

```text
add
set
get
on
use
open
send
load
save
```

Prefer verbs that describe the developer action. Avoid long method names that explain internal mechanics instead of intent.

Good:

```java
items.add("custom_item");
events.tick(...);
commands.add("home", ...);
network.channel("stats");
```

Avoid:

```java
Registry.register(Registries.ITEM, new Identifier("modid", "custom_item"), item);
```

## Chain depth

Avoid deep chains in common API calls.

```java
// Avoid
minecraft.client.getSave().world().state().time().sun();

// Prefer
world.time();
```

Short chains are acceptable when each step adds clear intent:

```java
items.add("custom_item")
    .name("Custom Item")
    .stack(16)
    .texture("special_texture");
```

## Defaults

Spindle should auto-fill boring fields for common declarations.

```java
items.add("custom_item");
```

This should deterministically infer something like:

```text
id: modid:custom_item
registry: item
stack: 64
display name: Custom Item
model path: assets/modid/models/item/custom_item.json
texture path: assets/modid/textures/item/custom_item.png
```

Defaults should reduce repetitive work. They should not hide identity, validation, or target behavior.

## Identity rules

Spindle must never silently change identity.

If `custom_item` conflicts, Spindle should report the conflict and fail or warn according to the relevant contract rules. It should not rename the item automatically, append a suffix, or silently redirect the declaration.

Good:

```text
modid:custom_item conflicts with othermod:custom_item or another declaration in the same namespace.
```

Bad:

```text
modid:custom_item was silently changed to modid:custom_item_2.
```

Identity must remain explicit, stable, and diagnosable.

## Explicit overrides

Defaults should be easy to override without leaving the simple API style.

```java
items.add("custom_item")
    .name("Custom Item")
    .stack(16)
    .texture("special_texture");
```

Common overrides should be close to the declaration. Advanced behavior can use more explicit builders or contract objects, but the common path should stay readable.

## Contract lowering

Public API calls should lower into deterministic internal contracts.

For example:

```java
items.add("custom_item");
```

Should lower into a contract containing:

```text
public declaration
resolved identity
registry target
defaulted properties
asset expectations
validation rules
conflict rules
diagnostics
target-lowering plan
```

The Minecraft Target Layer should handle the ugly target-specific details. The public Spindle API should not force ordinary developers to think in terms of obfuscated classes, mappings, raw registries, or launch internals.

## Advanced API rule

Advanced APIs are allowed, but they should be visibly advanced.

The common path should use names like:

```java
items.add("custom_item");
```

An advanced path may use more explicit names when the developer is intentionally taking control:

```java
items.add("custom_item")
    .contract(contract -> contract.requiresAsset("special_model"));
```

Advanced APIs should not leak into simple examples unless the feature truly requires them.

## Error and diagnostics naming

Diagnostics should use developer-facing language first and target-facing details second.

Prefer:

```text
Item 'modid:custom_item' conflicts with an existing item declaration.
```

Avoid making the primary message depend on internal mechanics:

```text
MappedRegistry insert failed for ResourceLocation modid:custom_item.
```

Internal details can appear in debug diagnostics, reports, or trace output.

## Final direction

```text
Intent first.
Minecraft mechanics second.
Short names.
No hidden identity changes.
Defaults are deterministic.
Advanced behavior is explicit.
The target layer handles target-specific complexity.
Simple public calls produce complete internal contracts.
```
