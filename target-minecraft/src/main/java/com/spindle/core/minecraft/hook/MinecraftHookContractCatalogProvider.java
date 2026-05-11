package com.spindle.core.minecraft.hook;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public final class MinecraftHookContractCatalogProvider {
  public MinecraftHookContractCatalog catalogFor(String minecraftVersion, MinecraftSide side) {
    if ("26.1.2".equals(minecraftVersion) && side == MinecraftSide.SERVER) {
      return minecraft2612ServerCatalog();
    }
    return MinecraftHookContractCatalog.empty();
  }

  private MinecraftHookContractCatalog minecraft2612ServerCatalog() {
    return new MinecraftHookContractCatalog(
        "minecraft-26.1.2-server-known-symbols",
        "Target-3 known-symbol hook contracts for Minecraft 26.1.2 server artifacts.",
        "26.1.2",
        MinecraftSide.SERVER,
        List.of(
            new MinecraftHookPointContract(
                "minecraft.26_1_2.server.main.class",
                "Known Minecraft 26.1.2 dedicated server entrypoint class.",
                MinecraftSide.SERVER,
                MinecraftHookPointKind.CLASS,
                "net/minecraft/server/Main",
                null,
                null,
                MinecraftHookRequirement.REQUIRED),
            new MinecraftHookPointContract(
                "minecraft.26_1_2.server.main.entrypoint",
                "Known Minecraft 26.1.2 dedicated server main entrypoint method.",
                MinecraftSide.SERVER,
                MinecraftHookPointKind.METHOD,
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                MinecraftHookRequirement.REQUIRED)));
  }
}
