# Config

Spindle config is a flat JSON contract exposed to mods through `ModConfig`.

Declare config entries in schema `2` `loader.mod.json`:

```json
{
  "permissions": ["config.read", "storage.config"],
  "storage": {
    "config": true
  },
  "config": {
    "runtimeWrites": false,
    "entries": [
      {"key": "enabled", "type": "boolean", "default": true},
      {"key": "mode", "type": "string", "default": "balanced", "allowed": ["balanced", "fast"]}
    ]
  }
}
```

Read values in lifecycle code:

```java
boolean enabled = context.config().getBoolean("enabled");
String mode = context.config().getString("mode");
```

Enable runtime writes only when the mod needs them:

```json
{
  "permissions": ["config.write", "storage.config"],
  "storage": {
    "config": true
  },
  "config": {
    "runtimeWrites": true,
    "entries": [
      {"key": "mode", "type": "string", "default": "balanced", "allowed": ["balanced", "fast"]}
    ]
  }
}
```

```java
context.config().setString("mode", "fast");
```

Rules:

- keys are flat only
- no arrays
- no nested objects
- no comments
- no TOML or YAML
- only declared keys are accessible
- getters must match the declared type
- `integer` means signed 32-bit only (`-2147483648` through `2147483647`)

Spindle stores the file at `config/<modId>/config.json`.

`ModConfig.keys()` returns deterministic sorted order when provided by Spindle.

Mod-facing config API failures use `ConfigAccessException`, including:

- undeclared key access
- wrong getter or setter type
- denied writes
- writes while `config.runtimeWrites` is false
- invalid runtime values
- non-finite `setNumber(...)` values such as `NaN`, `Infinity`, and `-Infinity`
- unavailable fallback config views such as `ModConfig.empty()`
- persistence failures while saving runtime writes

This config hardening pass does not change Runtime API version `1`, compiled profile schema `6`, or Runtime-4 config schema shape.
