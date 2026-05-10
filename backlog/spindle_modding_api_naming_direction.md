# Spindle Modding API Naming Direction

Spindle’s Modding API should prioritize human intent over Minecraft internals.

The API should read like what the developer wants to do, not like the engine path required to do it. Common actions should use short, obvious calls with stable defaults.

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

A short call should still expand into a full internal contract: ID, namespace, registry target, defaults, assets, validation rules, diagnostics, and target-lowering behavior.

## Naming rules

Use human words:

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
```

Avoid engine-heavy names in normal API calls:

```text
Registry.register
Registries.ITEM
Identifier
ResourceLocation
BootstrapContext
HolderLookup
MappedRegistry
```

Use short verbs:

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

Avoid deep chains:

```java
// Avoid
minecraft.client.getSave().world().state().time().sun();

// Prefer
world.time();
```

## Defaults

Spindle should auto-fill boring fields:

```java
items.add("custom_item");
```

Should infer:

```text
id: modid:custom_item
registry: item
stack: 64
display name: Custom Item
model path: assets/modid/models/item/custom_item.json
texture path: assets/modid/textures/item/custom_item.png
```

But Spindle should never silently change identity. If `custom_item` conflicts, report the conflict. Do not rename it automatically.

## Explicit overrides

Defaults should be easy to override:

```java
items.add("custom_item")
    .name("Custom Item")
    .stack(16)
    .texture("special_texture");
```

The API should stay simple for common cases and explicit for advanced cases.

## Final direction

```text
Intent first.
Minecraft mechanics second.
Short names.
No hidden identity changes.
Defaults are deterministic.
Advanced behavior is explicit.
The target layer handles the ugly parts.
```
