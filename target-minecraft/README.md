# Target Minecraft

`target-minecraft` is the current partial Minecraft Target Layer.

It owns Minecraft artifact handling, runtime planning, boundary reports, integration plans, bootstrap execution, and managed server-process helpers. It consumes the completed Spindle Loader subsystem but does not redefine the Spindle ecosystem brand or expose Minecraft behavior through the stable loader API.

## Future Target Layer API boundary

The next boundary-prep arc is documented in [Target Layer API Boundary](../docs/architecture/target-layer-api-boundary.md).

The first planned subsystem in that arc is the Injection Hook Subsystem, which remains inside `target-minecraft` and is not a standalone public API.
