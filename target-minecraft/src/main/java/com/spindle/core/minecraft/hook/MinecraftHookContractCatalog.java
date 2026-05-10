package com.spindle.core.minecraft.hook;

import java.util.List;

public record MinecraftHookContractCatalog(List<MinecraftHookPointContract> contracts) {
  public MinecraftHookContractCatalog {
    contracts = List.copyOf(contracts == null ? List.of() : contracts);
  }

  public static MinecraftHookContractCatalog empty() {
    return new MinecraftHookContractCatalog(List.of());
  }
}
