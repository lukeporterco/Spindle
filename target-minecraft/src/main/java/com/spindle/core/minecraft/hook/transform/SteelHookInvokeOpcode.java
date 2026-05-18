package com.spindle.core.minecraft.hook.transform;

public enum SteelHookInvokeOpcode {
  INVOKESTATIC("invokestatic", 0xb8),
  INVOKEVIRTUAL("invokevirtual", 0xb6),
  INVOKEINTERFACE("invokeinterface", 0xb9),
  INVOKESPECIAL("invokespecial", 0xb7),
  INVOKEDYNAMIC("invokedynamic", 0xba);

  private final String id;
  private final int opcode;

  SteelHookInvokeOpcode(String id, int opcode) {
    this.id = id;
    this.opcode = opcode;
  }

  public String id() {
    return id;
  }

  public int opcode() {
    return opcode;
  }
}
