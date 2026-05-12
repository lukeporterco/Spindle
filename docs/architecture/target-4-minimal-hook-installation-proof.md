# Target-4: Minimal Launch-Boundary Hook Installation Proof

Target-4 is the first internal hook installation pass in `target-minecraft`.

Its scope is deliberately narrow. Spindle validates the Target-3 known-symbol contract for Minecraft `26.1.2` server `net.minecraft.server.Main.main(String[])`, plans exactly one launch-boundary wrapper hook, writes a deterministic `minecraft-hook-installation-plan.json`, passes that plan into the bootstrap child JVM, invokes Minecraft main through an internal runtime bridge, and writes `minecraft-hook-installation-result.json`.

This is not bytecode injection.

## Exact Target-4 Hook

The only supported hook in this pass is:

- `id`: `target-4.minecraft.server.main.launch-boundary`
- `sourceContractId`: `minecraft.26_1_2.server.main.entrypoint`
- `catalogId`: `minecraft-26.1.2-server-known-symbols`
- `kind`: `LAUNCH_BOUNDARY_MAIN`
- `ownerInternalName`: `net/minecraft/server/Main`
- `memberName`: `main`
- `descriptor`: `([Ljava/lang/String;)V`
- `required`: `true`
- `mode`: `launch-boundary-main-wrapper`

No other hook ids, kinds, owners, members, descriptors, or modes are accepted in this pass.

## Planning Gate

Target-4 only plans installation when all of the following are true:

- the Target-3 catalog id is `minecraft-26.1.2-server-known-symbols`
- Target-3 validation passed
- Target-3 error count is `0`
- the Target-3 report contains a valid `minecraft.26_1_2.server.main.entrypoint` contract
- the frozen execution plan main class is `net.minecraft.server.Main`

Planning-only mode records gate failure in `minecraft-hook-installation-plan.json` without throwing.

Install mode fails closed if that gate does not pass.

## Runtime Behavior

The bootstrap child JVM verifies the frozen hook installation plan before Minecraft class loading.

When enabled, the runtime bridge:

- validates Target-4 plan schema and milestone
- validates that exactly one supported planned hook is present
- marks the launch-boundary hook as installed before Minecraft main class loading
- loads `net.minecraft.server.Main` through the existing runtime classloader
- resolves public static `main(String[])`
- invokes Minecraft main with the frozen execution plan arguments
- writes `minecraft-hook-installation-result.json`

The bridge does not rewrite, patch, remap, transform, or instrument Minecraft classes.

## What Target-4 Does Not Do

Target-4 does not:

- parse `Code` attributes
- inspect instructions or callsites
- use ASM, Byte Buddy, Java agents, Mixin, or remapping
- expose a public hook API
- add gameplay hooks
- imply Java mod sandboxing
- change normal bootstrap behavior when hooks are disabled
- put mods on the Minecraft runtime classpath

This pass proves a single internal launch-boundary wrapper around the validated server entrypoint and nothing more.
