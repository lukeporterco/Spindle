package com.spindle.core.runtime.config;

import java.util.List;

public record RuntimeConfigSummary(
    int mods,
    int entries,
    int valid,
    int defaulted,
    int invalid,
    int unknownKeys,
    int storageNotGranted,
    int fatalCount,
    int warningCount) {
  public static RuntimeConfigSummary fromMods(List<RuntimeConfigModPlan> mods) {
    int modCount = 0;
    int entries = 0;
    int valid = 0;
    int defaulted = 0;
    int invalid = 0;
    int unknownKeys = 0;
    int storageNotGranted = 0;
    int fatalCount = 0;
    int warningCount = 0;
    for (RuntimeConfigModPlan mod : mods) {
      modCount++;
      entries += mod.summary().entries();
      valid += mod.summary().valid();
      defaulted += mod.summary().defaulted();
      invalid += mod.summary().invalid();
      unknownKeys += mod.summary().unknownKeys();
      if (RuntimeConfigStates.MOD_STORAGE_NOT_GRANTED.equals(mod.state())) {
        storageNotGranted++;
      }
      fatalCount += mod.summary().fatalCount();
      warningCount += mod.summary().warningCount();
    }
    return new RuntimeConfigSummary(
        modCount, entries, valid, defaulted, invalid, unknownKeys, storageNotGranted, fatalCount, warningCount);
  }

  public static RuntimeConfigSummary modSummary(
      List<RuntimeConfigEntryPlan> entries, int unknownKeys, int fatalCount, int warningCount) {
    int valid = 0;
    int defaulted = 0;
    int invalid = 0;
    for (RuntimeConfigEntryPlan entry : entries) {
      switch (entry.state()) {
        case RuntimeConfigStates.ENTRY_VALID -> valid++;
        case RuntimeConfigStates.ENTRY_DEFAULTED, RuntimeConfigStates.ENTRY_MISSING_DEFAULTED -> defaulted++;
        case RuntimeConfigStates.ENTRY_INVALID_TYPE,
            RuntimeConfigStates.ENTRY_INVALID_RANGE,
            RuntimeConfigStates.ENTRY_INVALID_OPTION -> invalid++;
        default -> {}
      }
    }
    return new RuntimeConfigSummary(
        1, entries.size(), valid, defaulted, invalid, unknownKeys, 0, fatalCount, warningCount);
  }

  public static RuntimeConfigSummary empty() {
    return new RuntimeConfigSummary(0, 0, 0, 0, 0, 0, 0, 0, 0);
  }
}
