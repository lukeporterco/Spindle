# Capabilities

Runtime-2 capabilities describe Spindle-owned API surfaces, not process-level Java permissions.

Important:

- A granted capability means Spindle exposes that specific API surface.
- A denied capability means Spindle recognized the request but did not grant the API surface.
- An unavailable capability means the surface is planned but not implemented yet.
- A visibility-only capability means Spindle records the declaration but does not enforce it.
- Runtime-2 does not sandbox arbitrary Java.

## Current Grantable Capabilities

Runtime-2 can currently grant:

- `storage.config`
- `storage.data`
- `storage.cache`
- `storage.generated`

These are granted from the matching `storage` booleans in schema `2` metadata.

Example:

```json
{
  "permissions": [
    "storage.data",
    "storage.generated"
  ],
  "storage": {
    "data": true,
    "generated": true
  }
}
```

If `storage.generated` is `true`, Spindle grants `storage.generated` even if it is not listed in `permissions`.

## Current Unavailable Capabilities

Runtime-2 recognizes these future Spindle surfaces but does not implement them yet:

- `config.read`
- `config.write`
- `service.provide`
- `service.consume`
- `resource.declare`
- `resource.overlay`

## Current Visibility-Only Capabilities

Runtime-2 records these declarations for review only:

- `filesystem.read`
- `filesystem.write`
- `network.connect`
- `network.outbound`
- `process.spawn`
- `native.load`
- `reflection.deep`
- `unsafe.access`

These strings do not activate sandboxing or enforcement.
