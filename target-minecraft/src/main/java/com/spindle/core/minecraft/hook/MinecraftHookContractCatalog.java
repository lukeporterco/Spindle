package com.spindle.core.minecraft.hook;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftHookContractCatalog(
    String id,
    String description,
    String minecraftVersion,
    MinecraftSide side,
    List<MinecraftHookPointContract> contracts) {
  public MinecraftHookContractCatalog {
    contracts = List.copyOf(contracts == null ? List.of() : contracts);
  }

  public static MinecraftHookContractCatalog empty() {
    return new MinecraftHookContractCatalog(
        "empty",
        "No hook contracts declared for this Minecraft version and side.",
        null,
        null,
        List.of());
  }
}
