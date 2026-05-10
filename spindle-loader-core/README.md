# Spindle Loader Core

`spindle-loader-core` contains the target-neutral loader implementation.

It owns deterministic planning, metadata parsing, resolution, runtime contracts, diagnostics, security gates, classloading, and lifecycle execution. It does not own Minecraft target code, CLI wiring, or branding beyond the loader subsystem itself.
