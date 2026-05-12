package com.spindle.core.minecraft.hook.bytecode;

import com.spindle.core.diagnostics.LoaderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class MinecraftInstructionStreamDecoder {
  private static final String[] MNEMONICS = new String[256];
  private static final int[] FIXED_LENGTHS = new int[256];
  private static final MinecraftDecodedInstructionKind[] KINDS =
      new MinecraftDecodedInstructionKind[256];

  static {
    for (int opcode = 0; opcode < 256; opcode++) {
      FIXED_LENGTHS[opcode] = 1;
      KINDS[opcode] = MinecraftDecodedInstructionKind.UNSUPPORTED;
      MNEMONICS[opcode] = String.format("unsupported_0x%02x", opcode);
    }
    define(0x00, "nop", 1, MinecraftDecodedInstructionKind.SIMPLE);
    define(0x01, "aconst_null", 1, MinecraftDecodedInstructionKind.CONSTANT);
    defineRange(
        0x02,
        0x08,
        new String[] {
          "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5"
        },
        1,
        MinecraftDecodedInstructionKind.CONSTANT);
    defineRange(
        0x09,
        0x0a,
        new String[] {"lconst_0", "lconst_1"},
        1,
        MinecraftDecodedInstructionKind.CONSTANT);
    defineRange(
        0x0b,
        0x0d,
        new String[] {"fconst_0", "fconst_1", "fconst_2"},
        1,
        MinecraftDecodedInstructionKind.CONSTANT);
    defineRange(
        0x0e,
        0x0f,
        new String[] {"dconst_0", "dconst_1"},
        1,
        MinecraftDecodedInstructionKind.CONSTANT);
    define(0x10, "bipush", 2, MinecraftDecodedInstructionKind.CONSTANT);
    define(0x11, "sipush", 3, MinecraftDecodedInstructionKind.CONSTANT);
    define(0x12, "ldc", 2, MinecraftDecodedInstructionKind.CONSTANT);
    defineRange(
        0x13, 0x14, new String[] {"ldc_w", "ldc2_w"}, 3, MinecraftDecodedInstructionKind.CONSTANT);
    defineLoadStoreMnemonics();
    define(0x57, "pop", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x58, "pop2", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x59, "dup", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5a, "dup_x1", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5b, "dup_x2", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5c, "dup2", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5d, "dup2_x1", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5e, "dup2_x2", 1, MinecraftDecodedInstructionKind.STACK);
    define(0x5f, "swap", 1, MinecraftDecodedInstructionKind.STACK);
    defineArithmeticMnemonics();
    defineIncrementAndConversionMnemonics();
    define(0x94, "lcmp", 1, MinecraftDecodedInstructionKind.COMPARISON);
    define(0x95, "fcmpl", 1, MinecraftDecodedInstructionKind.COMPARISON);
    define(0x96, "fcmpg", 1, MinecraftDecodedInstructionKind.COMPARISON);
    define(0x97, "dcmpl", 1, MinecraftDecodedInstructionKind.COMPARISON);
    define(0x98, "dcmpg", 1, MinecraftDecodedInstructionKind.COMPARISON);
    defineBranchMnemonics();
    define(0xa9, "ret", 2, MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    define(0xaa, "tableswitch", -1, MinecraftDecodedInstructionKind.SWITCH);
    define(0xab, "lookupswitch", -1, MinecraftDecodedInstructionKind.SWITCH);
    defineRange(
        0xac,
        0xb1,
        new String[] {"ireturn", "lreturn", "freturn", "dreturn", "areturn", "return"},
        1,
        MinecraftDecodedInstructionKind.RETURN);
    define(0xb2, "getstatic", 3, MinecraftDecodedInstructionKind.FIELD_ACCESS);
    define(0xb3, "putstatic", 3, MinecraftDecodedInstructionKind.FIELD_ACCESS);
    define(0xb4, "getfield", 3, MinecraftDecodedInstructionKind.FIELD_ACCESS);
    define(0xb5, "putfield", 3, MinecraftDecodedInstructionKind.FIELD_ACCESS);
    define(0xb6, "invokevirtual", 3, MinecraftDecodedInstructionKind.INVOKE);
    define(0xb7, "invokespecial", 3, MinecraftDecodedInstructionKind.INVOKE);
    define(0xb8, "invokestatic", 3, MinecraftDecodedInstructionKind.INVOKE);
    define(0xb9, "invokeinterface", 5, MinecraftDecodedInstructionKind.INVOKE);
    define(0xba, "invokedynamic", 5, MinecraftDecodedInstructionKind.INVOKE);
    define(0xbb, "new", 3, MinecraftDecodedInstructionKind.OBJECT);
    define(0xbc, "newarray", 2, MinecraftDecodedInstructionKind.ARRAY);
    define(0xbd, "anewarray", 3, MinecraftDecodedInstructionKind.ARRAY);
    define(0xbe, "arraylength", 1, MinecraftDecodedInstructionKind.ARRAY);
    define(0xbf, "athrow", 1, MinecraftDecodedInstructionKind.THROW);
    define(0xc0, "checkcast", 3, MinecraftDecodedInstructionKind.TYPE);
    define(0xc1, "instanceof", 3, MinecraftDecodedInstructionKind.TYPE);
    define(0xc2, "monitorenter", 1, MinecraftDecodedInstructionKind.MONITOR);
    define(0xc3, "monitorexit", 1, MinecraftDecodedInstructionKind.MONITOR);
    define(0xc4, "wide", -1, MinecraftDecodedInstructionKind.WIDE);
    define(0xc5, "multianewarray", 4, MinecraftDecodedInstructionKind.ARRAY);
    define(0xc6, "ifnull", 3, MinecraftDecodedInstructionKind.BRANCH);
    define(0xc7, "ifnonnull", 3, MinecraftDecodedInstructionKind.BRANCH);
    define(0xc8, "goto_w", 5, MinecraftDecodedInstructionKind.BRANCH);
    define(0xc9, "jsr_w", 5, MinecraftDecodedInstructionKind.BRANCH);
    define(0xca, "breakpoint", 1, MinecraftDecodedInstructionKind.RESERVED);
    define(0xfe, "impdep1", 1, MinecraftDecodedInstructionKind.RESERVED);
    define(0xff, "impdep2", 1, MinecraftDecodedInstructionKind.RESERVED);
  }

  public Result decode(byte[] code) throws LoaderException {
    if (code == null) {
      throw new LoaderException("Bytecode array is required for instruction stream decoding.");
    }
    List<MinecraftDecodedInstruction> instructions = new ArrayList<>();
    boolean instructionBoundaryValidationPassed = true;
    boolean branchTargetValidationPassed = true;
    boolean switchTargetValidationPassed = true;
    String validationFailureReason = null;
    int returnInstructionCount = 0;
    int throwInstructionCount = 0;
    int invokeInstructionCount = 0;
    int branchInstructionCount = 0;
    int switchInstructionCount = 0;
    int wideInstructionCount = 0;
    int reservedOpcodeCount = 0;
    int unsupportedOpcodeCount = 0;
    int offset = 0;
    while (offset < code.length) {
      int opcode = Byte.toUnsignedInt(code[offset]);
      String mnemonic = MNEMONICS[opcode];
      MinecraftDecodedInstructionKind kind = KINDS[opcode];
      int fixedLength = FIXED_LENGTHS[opcode];
      DecodedInstructionData decoded;
      if (opcode == 0xaa) {
        decoded = decodeTableSwitch(code, offset);
      } else if (opcode == 0xab) {
        decoded = decodeLookupSwitch(code, offset);
      } else if (opcode == 0xc4) {
        decoded = decodeWide(code, offset);
      } else {
        decoded = decodeFixed(code, offset, opcode, fixedLength);
      }

      if (!decoded.valid()) {
        instructionBoundaryValidationPassed = false;
        if (validationFailureReason == null) {
          validationFailureReason = decoded.failureReason();
        }
      }
      if (kind == MinecraftDecodedInstructionKind.RESERVED) {
        instructionBoundaryValidationPassed = false;
        reservedOpcodeCount++;
        if (validationFailureReason == null) {
          validationFailureReason = "Reserved opcode " + mnemonic + " is not supported.";
        }
      } else if (kind == MinecraftDecodedInstructionKind.UNSUPPORTED) {
        instructionBoundaryValidationPassed = false;
        unsupportedOpcodeCount++;
        if (validationFailureReason == null) {
          validationFailureReason = "Unsupported opcode 0x" + toHexByte(opcode) + " was decoded.";
        }
      }

      instructions.add(
          new MinecraftDecodedInstruction(
              offset,
              opcode,
              mnemonic,
              decoded.length(),
              kind,
              decoded.operandHex(),
              decoded.branchTargetOffsets(),
              decoded.switchDefaultTargetOffset(),
              decoded.switchMatchTargetPairs(),
              decoded.wideModifiedOpcode()));
      returnInstructionCount += kind == MinecraftDecodedInstructionKind.RETURN ? 1 : 0;
      throwInstructionCount += kind == MinecraftDecodedInstructionKind.THROW ? 1 : 0;
      invokeInstructionCount += kind == MinecraftDecodedInstructionKind.INVOKE ? 1 : 0;
      branchInstructionCount += kind == MinecraftDecodedInstructionKind.BRANCH ? 1 : 0;
      switchInstructionCount += kind == MinecraftDecodedInstructionKind.SWITCH ? 1 : 0;
      wideInstructionCount += opcode == 0xc4 ? 1 : 0;
      offset += decoded.length();
    }

    Set<Integer> instructionOffsets = new TreeSet<>();
    for (MinecraftDecodedInstruction instruction : instructions) {
      instructionOffsets.add(instruction.offset());
    }
    for (MinecraftDecodedInstruction instruction : instructions) {
      for (Integer branchTargetOffset : instruction.branchTargetOffsets()) {
        if (!instructionOffsets.contains(branchTargetOffset)) {
          branchTargetValidationPassed = false;
          if (validationFailureReason == null) {
            validationFailureReason =
                "Branch target "
                    + branchTargetOffset
                    + " does not land on a decoded instruction boundary.";
          }
        }
      }
      if (instruction.switchDefaultTargetOffset() != null
          && !instructionOffsets.contains(instruction.switchDefaultTargetOffset())) {
        switchTargetValidationPassed = false;
        if (validationFailureReason == null) {
          validationFailureReason =
              "Switch default target "
                  + instruction.switchDefaultTargetOffset()
                  + " does not land on a decoded instruction boundary.";
        }
      }
      for (MinecraftDecodedBranchTarget switchTarget : instruction.switchMatchTargetPairs()) {
        if (!instructionOffsets.contains(switchTarget.targetOffset())) {
          switchTargetValidationPassed = false;
          if (validationFailureReason == null) {
            validationFailureReason =
                "Switch target "
                    + switchTarget.targetOffset()
                    + " does not land on a decoded instruction boundary.";
          }
        }
      }
    }

    return new Result(
        List.copyOf(instructions),
        instructionBoundaryValidationPassed,
        branchTargetValidationPassed,
        switchTargetValidationPassed,
        !instructions.isEmpty() && instructions.getFirst().offset() == 0,
        returnInstructionCount,
        throwInstructionCount,
        invokeInstructionCount,
        branchInstructionCount,
        switchInstructionCount,
        wideInstructionCount,
        reservedOpcodeCount,
        unsupportedOpcodeCount,
        validationFailureReason);
  }

  private DecodedInstructionData decodeFixed(byte[] code, int offset, int opcode, int fixedLength) {
    int available = code.length - offset;
    int length = fixedLength <= 0 ? 1 : Math.min(fixedLength, available);
    boolean valid = fixedLength > 0 && available >= fixedLength;
    String failureReason =
        valid
            ? null
            : "Truncated instruction " + MNEMONICS[opcode] + " at bytecode offset " + offset + ".";
    byte[] operandBytes = slice(code, offset + 1, length - 1);
    List<Integer> branchTargetOffsets = List.of();
    if ((opcode >= 0x99 && opcode <= 0xa8) || opcode == 0xc6 || opcode == 0xc7) {
      if (length >= 3) {
        int branchOffset = signedShort(code, offset + 1);
        branchTargetOffsets = List.of(offset + branchOffset);
      } else {
        valid = false;
        failureReason = "Truncated branch instruction at bytecode offset " + offset + ".";
      }
    } else if (opcode == 0xc8 || opcode == 0xc9) {
      if (length >= 5) {
        int branchOffset = signedInt(code, offset + 1);
        branchTargetOffsets = List.of(offset + branchOffset);
      } else {
        valid = false;
        failureReason = "Truncated wide branch instruction at bytecode offset " + offset + ".";
      }
    }
    return new DecodedInstructionData(
        length,
        toHex(operandBytes),
        branchTargetOffsets,
        null,
        List.of(),
        null,
        valid,
        failureReason);
  }

  private DecodedInstructionData decodeTableSwitch(byte[] code, int offset) {
    int padding = switchPadding(offset);
    int headerLength = 1 + padding + 12;
    int available = code.length - offset;
    if (available < headerLength) {
      return truncatedVariableInstruction(code, offset, "tableswitch");
    }
    int cursor = offset + 1 + padding;
    int defaultOffset = signedInt(code, cursor);
    int low = signedInt(code, cursor + 4);
    int high = signedInt(code, cursor + 8);
    long pairCount = (long) high - low + 1L;
    if (pairCount < 0L) {
      return invalidVariableInstruction(
          code,
          offset,
          "tableswitch",
          "Malformed tableswitch range at bytecode offset " + offset + ".");
    }
    long totalLengthLong = 1L + padding + 12L + pairCount * 4L;
    if (totalLengthLong > available) {
      return truncatedVariableInstruction(code, offset, "tableswitch");
    }
    int totalLength = (int) totalLengthLong;
    List<MinecraftDecodedBranchTarget> pairs = new ArrayList<>();
    int entriesCursor = cursor + 12;
    for (int matchValue = low; matchValue <= high; matchValue++) {
      pairs.add(
          new MinecraftDecodedBranchTarget(matchValue, offset + signedInt(code, entriesCursor)));
      entriesCursor += 4;
    }
    return new DecodedInstructionData(
        totalLength,
        toHex(slice(code, offset + 1, totalLength - 1)),
        List.of(),
        offset + defaultOffset,
        pairs,
        null,
        true,
        null);
  }

  private DecodedInstructionData decodeLookupSwitch(byte[] code, int offset) {
    int padding = switchPadding(offset);
    int headerLength = 1 + padding + 8;
    int available = code.length - offset;
    if (available < headerLength) {
      return truncatedVariableInstruction(code, offset, "lookupswitch");
    }
    int cursor = offset + 1 + padding;
    int defaultOffset = signedInt(code, cursor);
    int pairCount = signedInt(code, cursor + 4);
    if (pairCount < 0) {
      return invalidVariableInstruction(
          code,
          offset,
          "lookupswitch",
          "Malformed lookupswitch npairs at bytecode offset " + offset + ".");
    }
    long totalLengthLong = 1L + padding + 8L + (long) pairCount * 8L;
    if (totalLengthLong > available) {
      return truncatedVariableInstruction(code, offset, "lookupswitch");
    }
    int totalLength = (int) totalLengthLong;
    List<MinecraftDecodedBranchTarget> pairs = new ArrayList<>();
    int entriesCursor = cursor + 8;
    for (int index = 0; index < pairCount; index++) {
      int matchValue = signedInt(code, entriesCursor);
      int targetOffset = offset + signedInt(code, entriesCursor + 4);
      pairs.add(new MinecraftDecodedBranchTarget(matchValue, targetOffset));
      entriesCursor += 8;
    }
    return new DecodedInstructionData(
        totalLength,
        toHex(slice(code, offset + 1, totalLength - 1)),
        List.of(),
        offset + defaultOffset,
        pairs,
        null,
        true,
        null);
  }

  private DecodedInstructionData decodeWide(byte[] code, int offset) {
    int available = code.length - offset;
    if (available < 2) {
      return truncatedVariableInstruction(code, offset, "wide");
    }
    int modifiedOpcode = Byte.toUnsignedInt(code[offset + 1]);
    int totalLength;
    if (modifiedOpcode == 0x84) {
      totalLength = 6;
    } else if (isWideLoadStoreOrRet(modifiedOpcode)) {
      totalLength = 4;
    } else {
      return invalidVariableInstruction(
          code,
          offset,
          "wide",
          "Unsupported wide modified opcode 0x" + toHexByte(modifiedOpcode) + ".");
    }
    if (available < totalLength) {
      return truncatedVariableInstruction(code, offset, "wide");
    }
    return new DecodedInstructionData(
        totalLength,
        toHex(slice(code, offset + 1, totalLength - 1)),
        List.of(),
        null,
        List.of(),
        modifiedOpcode,
        true,
        null);
  }

  private DecodedInstructionData truncatedVariableInstruction(
      byte[] code, int offset, String mnemonic) {
    return new DecodedInstructionData(
        code.length - offset,
        toHex(slice(code, offset + 1, code.length - offset - 1)),
        List.of(),
        null,
        List.of(),
        null,
        false,
        "Truncated " + mnemonic + " instruction at bytecode offset " + offset + ".");
  }

  private DecodedInstructionData invalidVariableInstruction(
      byte[] code, int offset, String mnemonic, String failureReason) {
    return new DecodedInstructionData(
        code.length - offset,
        toHex(slice(code, offset + 1, code.length - offset - 1)),
        List.of(),
        null,
        List.of(),
        null,
        false,
        failureReason == null
            ? "Invalid " + mnemonic + " instruction at bytecode offset " + offset + "."
            : failureReason);
  }

  private static boolean isWideLoadStoreOrRet(int opcode) {
    return (opcode >= 0x15 && opcode <= 0x19)
        || (opcode >= 0x36 && opcode <= 0x3a)
        || opcode == 0xa9;
  }

  private static int switchPadding(int offset) {
    return (4 - ((offset + 1) & 3)) & 3;
  }

  private static int signedShort(byte[] bytes, int offset) {
    return (short) (((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff));
  }

  private static int signedInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 24)
        | ((bytes[offset + 1] & 0xff) << 16)
        | ((bytes[offset + 2] & 0xff) << 8)
        | (bytes[offset + 3] & 0xff);
  }

  private static byte[] slice(byte[] source, int offset, int length) {
    if (length <= 0 || offset >= source.length) {
      return new byte[0];
    }
    int boundedOffset = Math.max(offset, 0);
    int boundedLength = Math.min(length, source.length - boundedOffset);
    byte[] copy = new byte[boundedLength];
    System.arraycopy(source, boundedOffset, copy, 0, boundedLength);
    return copy;
  }

  private static String toHex(byte[] bytes) {
    if (bytes.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(toHexByte(Byte.toUnsignedInt(value)));
    }
    return builder.toString();
  }

  private static String toHexByte(int value) {
    return String.format("%02x", value);
  }

  private static void define(
      int opcode, String mnemonic, int length, MinecraftDecodedInstructionKind kind) {
    FIXED_LENGTHS[opcode] = length;
    KINDS[opcode] = kind;
    MNEMONICS[opcode] = mnemonic;
  }

  private static void defineRange(
      int startOpcode,
      int endOpcode,
      String[] mnemonics,
      int length,
      MinecraftDecodedInstructionKind kind) {
    for (int index = 0; index < mnemonics.length; index++) {
      define(startOpcode + index, mnemonics[index], length, kind);
    }
    if (startOpcode + mnemonics.length - 1 != endOpcode) {
      throw new IllegalStateException("Opcode mnemonic range size mismatch.");
    }
  }

  private static void defineLoadStoreMnemonics() {
    defineRange(
        0x15,
        0x19,
        new String[] {"iload", "lload", "fload", "dload", "aload"},
        2,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x1a,
        0x1d,
        new String[] {"iload_0", "iload_1", "iload_2", "iload_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x1e,
        0x21,
        new String[] {"lload_0", "lload_1", "lload_2", "lload_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x22,
        0x25,
        new String[] {"fload_0", "fload_1", "fload_2", "fload_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x26,
        0x29,
        new String[] {"dload_0", "dload_1", "dload_2", "dload_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x2a,
        0x2d,
        new String[] {"aload_0", "aload_1", "aload_2", "aload_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x2e,
        0x35,
        new String[] {
          "iaload", "laload", "faload", "daload", "aaload", "baload", "caload", "saload"
        },
        1,
        MinecraftDecodedInstructionKind.ARRAY);
    defineRange(
        0x36,
        0x3a,
        new String[] {"istore", "lstore", "fstore", "dstore", "astore"},
        2,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x3b,
        0x3e,
        new String[] {"istore_0", "istore_1", "istore_2", "istore_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x3f,
        0x42,
        new String[] {"lstore_0", "lstore_1", "lstore_2", "lstore_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x43,
        0x46,
        new String[] {"fstore_0", "fstore_1", "fstore_2", "fstore_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x47,
        0x4a,
        new String[] {"dstore_0", "dstore_1", "dstore_2", "dstore_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x4b,
        0x4e,
        new String[] {"astore_0", "astore_1", "astore_2", "astore_3"},
        1,
        MinecraftDecodedInstructionKind.LOCAL_VARIABLE);
    defineRange(
        0x4f,
        0x56,
        new String[] {
          "iastore", "lastore", "fastore", "dastore", "aastore", "bastore", "castore", "sastore"
        },
        1,
        MinecraftDecodedInstructionKind.ARRAY);
  }

  private static void defineArithmeticMnemonics() {
    defineRange(
        0x60,
        0x83,
        new String[] {
          "iadd", "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul",
          "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg",
          "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr", "iand", "land", "ior",
          "lor", "ixor", "lxor"
        },
        1,
        MinecraftDecodedInstructionKind.ARITHMETIC);
  }

  private static void defineIncrementAndConversionMnemonics() {
    define(0x84, "iinc", 3, MinecraftDecodedInstructionKind.ARITHMETIC);
    defineRange(
        0x85,
        0x93,
        new String[] {
          "i2l", "i2f", "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b",
          "i2c", "i2s"
        },
        1,
        MinecraftDecodedInstructionKind.CONVERSION);
  }

  private static void defineBranchMnemonics() {
    defineRange(
        0x99,
        0xa8,
        new String[] {
          "ifeq",
          "ifne",
          "iflt",
          "ifge",
          "ifgt",
          "ifle",
          "if_icmpeq",
          "if_icmpne",
          "if_icmplt",
          "if_icmpge",
          "if_icmpgt",
          "if_icmple",
          "if_acmpeq",
          "if_acmpne",
          "goto",
          "jsr"
        },
        3,
        MinecraftDecodedInstructionKind.BRANCH);
  }

  public record Result(
      List<MinecraftDecodedInstruction> instructions,
      boolean instructionBoundaryValidationPassed,
      boolean branchTargetValidationPassed,
      boolean switchTargetValidationPassed,
      boolean methodEntryInstructionBoundary,
      int returnInstructionCount,
      int throwInstructionCount,
      int invokeInstructionCount,
      int branchInstructionCount,
      int switchInstructionCount,
      int wideInstructionCount,
      int reservedOpcodeCount,
      int unsupportedOpcodeCount,
      String validationFailureReason) {
    public Result {
      instructions = List.copyOf(instructions == null ? List.of() : instructions);
    }
  }

  private record DecodedInstructionData(
      int length,
      String operandHex,
      List<Integer> branchTargetOffsets,
      Integer switchDefaultTargetOffset,
      List<MinecraftDecodedBranchTarget> switchMatchTargetPairs,
      Integer wideModifiedOpcode,
      boolean valid,
      String failureReason) {
    private DecodedInstructionData {
      branchTargetOffsets =
          List.copyOf(branchTargetOffsets == null ? List.of() : branchTargetOffsets);
      switchMatchTargetPairs =
          List.copyOf(switchMatchTargetPairs == null ? List.of() : switchMatchTargetPairs);
      operandHex = operandHex == null ? "" : operandHex;
    }
  }
}
