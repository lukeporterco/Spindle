package com.spindle.core.minecraft.interpret;

import java.util.List;

public record MinecraftInterpretedClass(
    String binaryName,
    String internalName,
    String packageName,
    String superName,
    List<String> interfaces,
    int access,
    List<String> accessFlags,
    List<MinecraftInterpretedField> fields,
    List<MinecraftInterpretedMethod> methods) {
  public MinecraftInterpretedClass {
    interfaces = List.copyOf(interfaces);
    accessFlags = List.copyOf(accessFlags);
    fields = List.copyOf(fields);
    methods = List.copyOf(methods);
  }
}
