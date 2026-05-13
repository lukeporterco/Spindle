package com.spindle.core.minecraft.command;

public enum MinecraftCommandDispatcherBindingStatus {
  SELECTED_SYMBOL_ANALYZED,
  NO_SYMBOL_TARGET,
  AMBIGUOUS_SYMBOL_TARGETS,
  UPSTREAM_GATE_BLOCKED,
  UNSUPPORTED_SYMBOL_KIND
}
