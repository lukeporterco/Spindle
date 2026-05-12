package com.spindle.core.minecraft.hook.bytecode;

import java.util.List;

public record MinecraftDecodedCodeAttribute(
    Integer maxStack,
    Integer maxLocals,
    Integer codeLength,
    String codeSha256,
    byte[] code,
    List<MinecraftDecodedExceptionHandler> exceptionHandlers,
    List<MinecraftCodeNestedAttributeSummary> nestedCodeAttributes,
    boolean hasCodeAttribute,
    boolean abstractOrNative,
    Integer methodEntryOffset,
    boolean stackMapTablePresent,
    Integer stackMapTableEntryCount) {
  public MinecraftDecodedCodeAttribute {
    code = code == null ? null : code.clone();
    exceptionHandlers = List.copyOf(exceptionHandlers == null ? List.of() : exceptionHandlers);
    nestedCodeAttributes =
        List.copyOf(nestedCodeAttributes == null ? List.of() : nestedCodeAttributes);
  }

  @Override
  public byte[] code() {
    return code == null ? null : code.clone();
  }
}
