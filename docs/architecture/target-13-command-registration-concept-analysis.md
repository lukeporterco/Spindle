# Target-13: Command Registration Concept Analysis

Target-13 is analysis-only.

This pass identifies where command registration can be represented in the Minecraft Target Layer once future Minecraft command dispatcher symbols are known.

The only available upstream anchor in this pass is Target-12's symbolic `minecraft.server.lifecycle.starting` dispatch. That upstream lifecycle anchor does not mean a command dispatcher is known, commands can be registered, reload is available, or the server is ready for command mutation.

## Output

Target-13 writes one deterministic report:

- `minecraft-command-registration-analysis.json`

## Boundaries In This Pass

The following five boundaries are always reported:

- `minecraft.commands.lifecycle_anchor`
- `minecraft.commands.dispatcher.discovery`
- `minecraft.commands.registration.window`
- `minecraft.commands.registration.apply`
- `minecraft.commands.reload.reapply`

Only `minecraft.commands.lifecycle_anchor` can become available in this pass, and only when Target-12 planned `target-12.minecraft.server.lifecycle.starting.dispatch`.

No Minecraft command dispatcher symbol is bound in this pass.

The remaining four command registration boundaries stay declared but unbound:

- dispatcher discovery
- registration window
- registration apply
- reload reapply

Target-14 follows this pass by scanning Target-1 interpreted metadata for deterministic Brigadier `CommandDispatcher` descriptor references. That later pass may determine future proof eligibility, but it still does not perform command registration.

## What This Pass Does Not Add

No Brigadier adapter is added.
No command registration occurs.
No command execution occurs.
No command tree is read or mutated.
No public command API exists.
No public Modding API exists.
No new SteelHook primitive is added.
No runtime callback is added.
Java mod execution is not sandboxed.

This pass does not add:

- Brigadier dependencies
- command dispatcher discovery against real Minecraft symbols
- command dispatcher access or mutation
- command tree APIs
- public `com.spindle.api.minecraft.*`
- runtime callbacks
- hook placement
- bytecode analysis
- patch planning
- hook installation
- bootstrap behavior
- runtime transformation
- real Minecraft runtime artifact transformation
- `StackMapTable` rewriting
- registry/content registration
- data generation tooling
- networking
- client support
