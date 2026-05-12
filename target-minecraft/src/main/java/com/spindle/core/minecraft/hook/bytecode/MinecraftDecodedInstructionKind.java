package com.spindle.core.minecraft.hook.bytecode;

public enum MinecraftDecodedInstructionKind {
  SIMPLE,
  CONSTANT,
  LOCAL_VARIABLE,
  FIELD_ACCESS,
  INVOKE,
  BRANCH,
  SWITCH,
  RETURN,
  THROW,
  TYPE,
  OBJECT,
  ARRAY,
  STACK,
  ARITHMETIC,
  CONVERSION,
  COMPARISON,
  MONITOR,
  WIDE,
  RESERVED,
  UNSUPPORTED
}
