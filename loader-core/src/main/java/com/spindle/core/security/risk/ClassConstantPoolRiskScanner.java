package com.spindle.core.security.risk;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class ClassConstantPoolRiskScanner {
  private static final int CLASS_MAGIC = 0xCAFEBABE;

  public ScanResult scan(byte[] classBytes) {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
      if (input.readInt() != CLASS_MAGIC) {
        return ScanResult.failure("does not start with CAFEBABE class magic");
      }
      input.readUnsignedShort();
      input.readUnsignedShort();
      int constantPoolCount = input.readUnsignedShort();
      TreeSet<String> utf8Constants = new TreeSet<>();
      for (int index = 1; index < constantPoolCount; index++) {
        int tag = input.readUnsignedByte();
        switch (tag) {
          case 1 -> utf8Constants.add(input.readUTF());
          case 3, 4 -> input.skipNBytes(4);
          case 5, 6 -> {
            input.skipNBytes(8);
            index++;
          }
          case 7, 8, 16, 19, 20 -> input.skipNBytes(2);
          case 9, 10, 11, 12, 17, 18 -> input.skipNBytes(4);
          case 15 -> input.skipNBytes(3);
          default -> {
            return ScanResult.failure("uses unsupported constant-pool tag " + tag);
          }
        }
      }
      return ScanResult.success(new ArrayList<>(utf8Constants));
    } catch (EOFException exception) {
      return ScanResult.failure("ends before the constant pool could be read");
    } catch (IOException exception) {
      return ScanResult.failure("could not be read: " + exception.getMessage());
    }
  }

  public record ScanResult(List<String> utf8Constants, String warningReason) {
    static ScanResult success(List<String> utf8Constants) {
      return new ScanResult(List.copyOf(utf8Constants), null);
    }

    static ScanResult failure(String warningReason) {
      return new ScanResult(List.of(), warningReason);
    }

    public boolean hasWarning() {
      return warningReason != null;
    }
  }
}
