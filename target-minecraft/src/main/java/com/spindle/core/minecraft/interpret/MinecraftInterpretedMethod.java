package com.spindle.core.minecraft.interpret;

import java.util.List;

public record MinecraftInterpretedMethod(
    String name,
    String descriptor,
    int access,
    List<String> accessFlags,
    boolean constructor,
    boolean staticMethod) {
  public MinecraftInterpretedMethod {
    accessFlags = List.copyOf(accessFlags);
  }
}
