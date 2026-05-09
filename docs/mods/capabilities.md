# Capabilities

Runtime-3 capabilities describe Spindle-owned API surfaces, not process-level Java permissions.

Important:

- granted means Spindle exposes a specific loader-owned API surface
- denied means Spindle recognized the request but metadata did not satisfy the contract
- unavailable means the surface is planned but not implemented yet
- visibility-only means Spindle records the declaration but does not enforce it
- Runtime-3 does not sandbox arbitrary Java

## Current Grantable Capabilities

Runtime-3 can grant:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`
- `service.provide`
- `service.consume`

Storage grants derive from schema `2` `storage` booleans.

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

## Current Unavailable Capabilities

Runtime-3 still reserves these future surfaces:

- `config.read`
- `config.write`
- `resource.declare`
- `resource.overlay`

## Current Visibility-Only Capabilities

Runtime-3 records these declarations for review only:

- `filesystem.read`
- `filesystem.write`
- `network.connect`
- `network.outbound`
- `process.spawn`
- `native.load`
- `reflection.deep`
- `unsafe.access`

These strings do not activate sandboxing or enforcement.
