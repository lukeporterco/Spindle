# Capabilities

Runtime-4 capabilities describe Spindle-owned API surfaces, not process-level Java permissions.

Important:

- granted means Spindle exposes a specific loader-owned API surface
- denied means Spindle recognized the request but metadata did not satisfy the contract
- unavailable means the surface is planned but not implemented yet
- visibility-only means Spindle records the declaration but does not enforce it
- Runtime-4 does not sandbox arbitrary Java

## Current Grantable Capabilities

Runtime-4 can grant:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`
- `config.read`
- `config.write`
- `service.provide`
- `service.consume`

Storage grants derive from schema `2` `storage` booleans.

Config grants derive from schema `2` `config` declarations plus `storage.config`.

Service grants derive from schema `2` `services` declarations.

Example:

```json
{
  "permissions": [
    "service.consume",
    "storage.generated"
  ],
  "storage": {
    "generated": true
  },
  "services": {
    "consumes": [
      {
        "id": "sample:greeting",
        "type": "com.example.api.GreetingService",
        "required": true
      }
    ]
  }
}
```

If a matching `storage` or `services` declaration exists, Spindle grants the capability even if it is omitted from `permissions`.

For config:

- `config.read` is granted when the mod declares at least one `config.entries` entry and enables `storage.config`
- `config.write` is granted when the mod also sets `config.runtimeWrites: true`

## Current Unavailable Capabilities

Runtime-4 still reserves these future surfaces:

- `resource.declare`
- `resource.overlay`

## Current Visibility-Only Capabilities

Runtime-4 records these declarations for review only:

- `filesystem.read`
- `filesystem.write`
- `network.connect`
- `network.outbound`
- `process.spawn`
- `native.load`
- `reflection.deep`
- `unsafe.access`

These strings do not activate sandboxing or enforcement.
