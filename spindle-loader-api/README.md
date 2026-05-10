# Spindle Loader API

`spindle-loader-api` is the stable runtime-facing Spindle Loader API.

It contains the public `com.spindle.api.*` surface that mods use today. It stays small, boring, and stable. Spindle remains the ecosystem brand; `spindle-loader-api` names the loader subsystem module specifically.

`com.spindle.api.minecraft.*` remains deferred bootstrap-facing surface area. It is not part of the stabilized Spindle Loader runtime API.
