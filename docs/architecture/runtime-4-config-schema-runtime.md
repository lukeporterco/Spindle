# Runtime-4 Config Schema Runtime

Runtime-4 turns `config.read` and `config.write` into real Spindle-owned capabilities.

Scope:

- schema `2` metadata may declare flat config entries in `loader.mod.json`
- Spindle validates and materializes `config/<modId>/config.json` before lifecycle execution
- lifecycle code reads and writes declared keys through `ModContext.config()`
- fatal config findings block standard runtime execution before classloading

Runtime-4 intentionally does not add:

- nested schemas
- arrays or objects
- migrations
- external schema files
- comments
- UI
- client or server sync
- sandboxing

## Metadata Contract

Schema `2` adds optional `config`:

```json
{
  "config": {
    "runtimeWrites": false,
    "entries": [
      {"key": "enabled", "type": "boolean", "default": true},
      {"key": "maxcount", "type": "integer", "default": 8, "min": 0, "max": 64},
      {"key": "scale", "type": "number", "default": 1.0, "min": 0.1, "max": 10.0},
      {"key": "mode", "type": "string", "default": "balanced", "allowed": ["balanced", "fast"]}
    ]
  }
}
```

Rules:

- `config` is schema `2` only
- `key` must match `[a-z][a-z0-9_-]{0,63}`
- only `boolean`, `integer`, `number`, and `string` are supported
- string values may use `allowed`
- numeric values may use `min` and `max`
- values are represented canonically as strings inside the compiled runtime contract

## Materialization Contract

Runtime-4 owns `config/<modId>/config.json`.

Behavior:

- missing file: Spindle writes defaults and records a warning
- missing declared keys: Spindle preserves valid keys, adds defaults, and records a warning
- unknown keys: Spindle preserves them and records a warning
- invalid JSON, invalid type, invalid range, or invalid option: Spindle records a fatal finding and does not rewrite the file
- declaring config entries without `storage.config: true`: fatal `storage-not-granted`

## Runtime API Contract

`ModContext.config()` exposes only declared keys.

- undeclared key access throws
- wrong typed getter throws
- setters require granted `config.write`
- setters also require `config.runtimeWrites: true`
- successful writes persist the owned config file lazily

Runtime-4 does not change runtime honesty:

- execution remains `in-process-unrestricted-java`
- arbitrary runtime Java is still not sandboxed
