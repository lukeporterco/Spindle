package com.spindle.core.minecraft.hook.transform;

public enum SteelHookMethodExitReturnOpcode {
  IRETURN(0xac, "ac", "ireturn", DescriptorCategory.INT_LIKE, 1),
  LRETURN(0xad, "ad", "lreturn", DescriptorCategory.LONG, 1),
  FRETURN(0xae, "ae", "freturn", DescriptorCategory.FLOAT, 1),
  DRETURN(0xaf, "af", "dreturn", DescriptorCategory.DOUBLE, 1),
  ARETURN(0xb0, "b0", "areturn", DescriptorCategory.REFERENCE, 1),
  RETURN(0xb1, "b1", "return", DescriptorCategory.VOID, 1);

  private final int opcode;
  private final String opcodeHex;
  private final String mnemonic;
  private final DescriptorCategory descriptorCategory;
  private final int byteLength;

  SteelHookMethodExitReturnOpcode(
      int opcode,
      String opcodeHex,
      String mnemonic,
      DescriptorCategory descriptorCategory,
      int byteLength) {
    this.opcode = opcode;
    this.opcodeHex = opcodeHex;
    this.mnemonic = mnemonic;
    this.descriptorCategory = descriptorCategory;
    this.byteLength = byteLength;
  }

  public int opcode() {
    return opcode;
  }

  public String opcodeHex() {
    return opcodeHex;
  }

  public String mnemonic() {
    return mnemonic;
  }

  public DescriptorCategory descriptorCategory() {
    return descriptorCategory;
  }

  public int byteLength() {
    return byteLength;
  }

  public static SteelHookMethodExitReturnOpcode fromOpcode(int opcode) {
    for (SteelHookMethodExitReturnOpcode value : values()) {
      if (value.opcode == opcode) {
        return value;
      }
    }
    return null;
  }

  public enum DescriptorCategory {
    VOID,
    INT_LIKE,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE
  }
}
