package com.spindle.core.minecraft.interpret;

import java.util.List;

public record MinecraftInterpretedField(
    String name, String descriptor, int access, List<String> accessFlags) {
  public MinecraftInterpretedField {
    accessFlags = List.copyOf(accessFlags);
  }
}
