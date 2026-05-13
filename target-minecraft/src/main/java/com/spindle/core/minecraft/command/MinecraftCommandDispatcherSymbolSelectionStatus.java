package com.spindle.core.minecraft.command;

public enum MinecraftCommandDispatcherSymbolSelectionStatus {
  STABLE_TARGET_SELECTED,
  NO_CANDIDATES,
  AMBIGUOUS_CANDIDATES,
  UPSTREAM_GATE_BLOCKED
}
