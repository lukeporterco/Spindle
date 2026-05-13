# Spindle Loader API

`spindle-loader-api` is the stable runtime-facing Spindle Loader API.

It contains the public `com.spindle.api.*` surface that mods use today. It stays small, boring, and stable. Spindle remains the ecosystem brand; `spindle-loader-api` names the loader subsystem module specifically.

`com.spindle.api.minecraft.*` currently contains deferred bootstrap-facing placeholder interfaces used by guarded Minecraft bootstrap fixtures. It is excluded from the stable Loader API today, is not part of the stabilized Runtime API-0 boundary, and is not the public Minecraft Modding API. It remains only a deferred placeholder area for future Minecraft-facing APIs after explicit boundary passes, including a low-level target namespace if one is needed.
