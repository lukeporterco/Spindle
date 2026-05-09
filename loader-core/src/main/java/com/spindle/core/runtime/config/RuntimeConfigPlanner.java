package com.spindle.core.runtime.config;

import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeConfigPlanner {
  public RuntimeConfigContract plan(ResolvedModSet resolvedMods) {
    List<RuntimeConfigModPlan> mods = new ArrayList<>();
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      mods.add(planMod(mod));
    }
    return new RuntimeConfigContract(
        RuntimeConfigContract.CONTRACT_VERSION, RuntimeConfigContract.SCOPE, RuntimeConfigContract.FORMAT, mods, null);
  }

  private RuntimeConfigModPlan planMod(ResolvedModSet.ResolvedMod mod) {
    ModMetadata.Config config = mod.config();
    if (config.entries().isEmpty()) {
      return new RuntimeConfigModPlan(
          mod.id(),
          "config/" + mod.id() + "/config.json",
          false,
          RuntimeConfigStates.MOD_NONE,
          List.of(),
          List.of(),
          RuntimeConfigSummary.modSummary(List.of(), 0, 0, 0),
          null);
    }

    List<RuntimeConfigEntryPlan> entries =
        config.entries().stream()
            .map(
                entry ->
                    new RuntimeConfigEntryPlan(
                        entry.key(),
                        entry.type(),
                        entry.defaultValue(),
                        entry.defaultValue(),
                        RuntimeConfigStates.ENTRY_VALID,
                        "Config default is declared.",
                        entry.min(),
                        entry.max(),
                        entry.allowed(),
                        null))
            .toList();
    if (!mod.storage().config()) {
      return new RuntimeConfigModPlan(
          mod.id(),
          "config/" + mod.id() + "/config.json",
          config.runtimeWrites(),
          RuntimeConfigStates.MOD_STORAGE_NOT_GRANTED,
          entries,
          List.of(),
          RuntimeConfigSummary.modSummary(entries, 0, 1, 0),
          "config.storage_not_granted");
    }
    return new RuntimeConfigModPlan(
        mod.id(),
        "config/" + mod.id() + "/config.json",
        config.runtimeWrites(),
        RuntimeConfigStates.MOD_VALID,
        entries,
        List.of(),
        RuntimeConfigSummary.modSummary(entries, 0, 0, 0),
        null);
  }
}
