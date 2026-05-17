package com.spindle.core.minecraft.hook.transform;

import java.io.ByteArrayOutputStream;

public final class SteelHookStackMapTableRewriter {
  private static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247;
  private static final int SAME_FRAME_EXTENDED = 251;
  private static final int FULL_FRAME = 255;

  public SteelHookStackMapTableRewriteResult rewrite(
      byte[] stackMapTableAttributeBody, int insertedInstructionLength, String scope) {
    byte[] body = stackMapTableAttributeBody == null ? null : stackMapTableAttributeBody.clone();
    String resolvedScope = scope == null || scope.isBlank() ? "StackMapTable rewrite" : scope;
    if (body == null) {
      return rejected("StackMapTable attribute body is required for " + resolvedScope + ".", 0);
    }
    if (insertedInstructionLength < 0) {
      return rejected(
          "Inserted instruction length must be non-negative for " + resolvedScope + ".",
          body.length);
    }
    try {
      Reader reader = new Reader(body, resolvedScope);
      ByteArrayOutputStream rewritten = new ByteArrayOutputStream(body.length + 4);
      int entryCount = reader.readUnsignedShort();
      writeUnsignedShort(rewritten, entryCount);
      if (entryCount == 0) {
        ensureFullyConsumed(reader);
        return new SteelHookStackMapTableRewriteResult(
            SteelHookStackMapTableRewriteStatus.PRESERVED,
            null,
            0,
            0,
            false,
            null,
            null,
            body.length,
            body.length,
            body);
      }

      FrameMetadata firstFrame = parseFrame(reader, 0, insertedInstructionLength, rewritten);
      for (int index = 1; index < entryCount; index++) {
        int frameStart = reader.position();
        parseFrame(reader, index, insertedInstructionLength, null);
        rewritten.write(body, frameStart, reader.position() - frameStart);
      }
      ensureFullyConsumed(reader);
      byte[] transformedBody = rewritten.toByteArray();
      return new SteelHookStackMapTableRewriteResult(
          firstFrame.rewriteApplied
              ? SteelHookStackMapTableRewriteStatus.REWRITTEN
              : SteelHookStackMapTableRewriteStatus.PRESERVED,
          null,
          entryCount,
          entryCount,
          firstFrame.rewriteApplied,
          firstFrame.originalOffsetDelta,
          firstFrame.transformedOffsetDelta,
          body.length,
          transformedBody.length,
          transformedBody);
    } catch (RewriteException exception) {
      return rejected(exception.getMessage(), body.length);
    }
  }

  private FrameMetadata parseFrame(
      Reader reader,
      int frameIndex,
      int insertedInstructionLength,
      ByteArrayOutputStream rewrittenOutput)
      throws RewriteException {
    int frameType = reader.readUnsignedByte();
    if (frameType <= 63) {
      int originalOffsetDelta = frameType;
      if (frameIndex == 0) {
        int transformedOffsetDelta =
            shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope());
        if (rewrittenOutput != null) {
          if (transformedOffsetDelta <= 63) {
            rewrittenOutput.write(transformedOffsetDelta);
          } else {
            rewrittenOutput.write(SAME_FRAME_EXTENDED);
            writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
          }
        }
        return new FrameMetadata(true, originalOffsetDelta, transformedOffsetDelta);
      }
      return new FrameMetadata(false, originalOffsetDelta, originalOffsetDelta);
    }
    if (frameType <= 127) {
      int originalOffsetDelta = frameType - 64;
      byte[] verificationInfo = readVerificationTypeInfo(reader);
      if (frameIndex == 0) {
        int transformedOffsetDelta =
            shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope());
        if (rewrittenOutput != null) {
          if (transformedOffsetDelta <= 63) {
            rewrittenOutput.write(64 + transformedOffsetDelta);
          } else {
            rewrittenOutput.write(SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED);
            writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
          }
          rewrittenOutput.writeBytes(verificationInfo);
        }
        return new FrameMetadata(true, originalOffsetDelta, transformedOffsetDelta);
      }
      return new FrameMetadata(false, originalOffsetDelta, originalOffsetDelta);
    }
    if (frameType >= 128 && frameType <= 246) {
      throw new RewriteException(
          "Unsupported StackMapTable frame_type " + frameType + " for " + reader.scope() + ".");
    }
    if (frameType == SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED) {
      int originalOffsetDelta = reader.readUnsignedShort();
      byte[] verificationInfo = readVerificationTypeInfo(reader);
      int transformedOffsetDelta =
          frameIndex == 0
              ? shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope())
              : originalOffsetDelta;
      if (frameIndex == 0 && rewrittenOutput != null) {
        rewrittenOutput.write(frameType);
        writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
        rewrittenOutput.writeBytes(verificationInfo);
      }
      return new FrameMetadata(frameIndex == 0, originalOffsetDelta, transformedOffsetDelta);
    }
    if (frameType >= 248 && frameType <= 250) {
      return rewriteOffsetOnlyFrame(
          reader, frameIndex, insertedInstructionLength, rewrittenOutput, frameType);
    }
    if (frameType == SAME_FRAME_EXTENDED) {
      return rewriteOffsetOnlyFrame(
          reader, frameIndex, insertedInstructionLength, rewrittenOutput, frameType);
    }
    if (frameType >= 252 && frameType <= 254) {
      int originalOffsetDelta = reader.readUnsignedShort();
      int localCount = frameType - 251;
      byte[] locals = readVerificationTypeInfos(reader, localCount);
      int transformedOffsetDelta =
          frameIndex == 0
              ? shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope())
              : originalOffsetDelta;
      if (frameIndex == 0 && rewrittenOutput != null) {
        rewrittenOutput.write(frameType);
        writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
        rewrittenOutput.writeBytes(locals);
      }
      return new FrameMetadata(frameIndex == 0, originalOffsetDelta, transformedOffsetDelta);
    }
    if (frameType == FULL_FRAME) {
      int originalOffsetDelta = reader.readUnsignedShort();
      int localCount = reader.readUnsignedShort();
      byte[] locals = readVerificationTypeInfos(reader, localCount);
      int stackCount = reader.readUnsignedShort();
      byte[] stack = readVerificationTypeInfos(reader, stackCount);
      int transformedOffsetDelta =
          frameIndex == 0
              ? shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope())
              : originalOffsetDelta;
      if (frameIndex == 0 && rewrittenOutput != null) {
        rewrittenOutput.write(frameType);
        writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
        writeUnsignedShort(rewrittenOutput, localCount);
        rewrittenOutput.writeBytes(locals);
        writeUnsignedShort(rewrittenOutput, stackCount);
        rewrittenOutput.writeBytes(stack);
      }
      return new FrameMetadata(frameIndex == 0, originalOffsetDelta, transformedOffsetDelta);
    }
    throw new RewriteException(
        "Unsupported StackMapTable frame_type " + frameType + " for " + reader.scope() + ".");
  }

  private FrameMetadata rewriteOffsetOnlyFrame(
      Reader reader,
      int frameIndex,
      int insertedInstructionLength,
      ByteArrayOutputStream rewrittenOutput,
      int frameType)
      throws RewriteException {
    int originalOffsetDelta = reader.readUnsignedShort();
    int transformedOffsetDelta =
        frameIndex == 0
            ? shiftedOffsetDelta(originalOffsetDelta, insertedInstructionLength, reader.scope())
            : originalOffsetDelta;
    if (frameIndex == 0 && rewrittenOutput != null) {
      rewrittenOutput.write(frameType);
      writeUnsignedShort(rewrittenOutput, transformedOffsetDelta);
    }
    return new FrameMetadata(frameIndex == 0, originalOffsetDelta, transformedOffsetDelta);
  }

  private byte[] readVerificationTypeInfos(Reader reader, int count) throws RewriteException {
    if (count < 0) {
      throw new RewriteException(
          "Malformed verification_type_info count for " + reader.scope() + ".");
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(count * 3);
    for (int index = 0; index < count; index++) {
      bytes.writeBytes(readVerificationTypeInfo(reader));
    }
    return bytes.toByteArray();
  }

  private byte[] readVerificationTypeInfo(Reader reader) throws RewriteException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(3);
    int tag = reader.readUnsignedByte();
    bytes.write(tag);
    switch (tag) {
      case 0, 1, 2, 3, 4, 5, 6 -> {
        return bytes.toByteArray();
      }
      case 7, 8 -> {
        int value = reader.readUnsignedShort();
        writeUnsignedShort(bytes, value);
        return bytes.toByteArray();
      }
      default ->
          throw new RewriteException(
              "Unknown StackMapTable verification_type_info tag "
                  + tag
                  + " for "
                  + reader.scope()
                  + ".");
    }
  }

  private int shiftedOffsetDelta(
      int originalOffsetDelta, int insertedInstructionLength, String scope)
      throws RewriteException {
    int transformedOffsetDelta = originalOffsetDelta + insertedInstructionLength;
    if (transformedOffsetDelta > 0xFFFF) {
      throw new RewriteException(
          "Shifted StackMapTable first frame offset_delta exceeds 65535 for " + scope + ".");
    }
    return transformedOffsetDelta;
  }

  private void ensureFullyConsumed(Reader reader) throws RewriteException {
    if (!reader.isFullyConsumed()) {
      throw new RewriteException(
          "Trailing bytes remain in StackMapTable for " + reader.scope() + ".");
    }
  }

  private SteelHookStackMapTableRewriteResult rejected(
      String failureReason, int originalBodyLength) {
    return new SteelHookStackMapTableRewriteResult(
        SteelHookStackMapTableRewriteStatus.REJECTED,
        failureReason,
        0,
        0,
        false,
        null,
        null,
        originalBodyLength,
        originalBodyLength,
        null);
  }

  private static void writeUnsignedShort(ByteArrayOutputStream output, int value) {
    output.write((value >>> 8) & 0xFF);
    output.write(value & 0xFF);
  }

  private record FrameMetadata(
      boolean rewriteApplied, Integer originalOffsetDelta, Integer transformedOffsetDelta) {}

  private static final class Reader {
    private final byte[] bytes;
    private final String scope;
    private int position;

    private Reader(byte[] bytes, String scope) {
      this.bytes = bytes;
      this.scope = scope;
    }

    private String scope() {
      return scope;
    }

    private int position() {
      return position;
    }

    private int readUnsignedByte() throws RewriteException {
      require(1);
      return bytes[position++] & 0xFF;
    }

    private int readUnsignedShort() throws RewriteException {
      require(2);
      int value = ((bytes[position] & 0xFF) << 8) | (bytes[position + 1] & 0xFF);
      position += 2;
      return value;
    }

    private boolean isFullyConsumed() {
      return position == bytes.length;
    }

    private void require(int byteCount) throws RewriteException {
      if (byteCount < 0 || position + byteCount > bytes.length) {
        throw new RewriteException("Malformed StackMapTable attribute body for " + scope + ".");
      }
    }
  }

  private static final class RewriteException extends Exception {
    private RewriteException(String message) {
      super(message);
    }
  }
}
