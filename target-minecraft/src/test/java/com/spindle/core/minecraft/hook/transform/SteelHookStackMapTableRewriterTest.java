package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class SteelHookStackMapTableRewriterTest {
  private final SteelHookStackMapTableRewriter rewriter = new SteelHookStackMapTableRewriter();

  @Test
  void emptyStackMapTableBodyWithNumberOfEntriesZeroIsPreserved() {
    byte[] body = bytes(0x00, 0x00);

    SteelHookStackMapTableRewriteResult result = rewriter.rewrite(body, 3, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.PRESERVED, result.status());
    assertFalse(result.rewriteApplied());
    assertEquals(0, result.originalEntryCount());
    assertEquals(0, result.transformedEntryCount());
    assertNull(result.firstFrameOffsetDeltaBefore());
    assertNull(result.firstFrameOffsetDeltaAfter());
    assertArrayEquals(body, result.transformedBody());
  }

  @Test
  void sameFrameFirstFrameShiftsByInsertedInstructionLength() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0x05), 3, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REWRITTEN, result.status());
    assertTrue(result.rewriteApplied());
    assertEquals(5, result.firstFrameOffsetDeltaBefore());
    assertEquals(8, result.firstFrameOffsetDeltaAfter());
    assertArrayEquals(bytes(0x00, 0x01, 0x08), result.transformedBody());
  }

  @Test
  void sameFrameExpandsToSameFrameExtendedWhenShiftedOffsetExceedsSixtyThree() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0x3f), 1, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REWRITTEN, result.status());
    assertArrayEquals(bytes(0x00, 0x01, 0xfb, 0x00, 0x40), result.transformedBody());
  }

  @Test
  void sameLocalsOneStackItemFrameFirstFrameShiftsWhilePreservingVerificationInfo() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0x45, 0x07, 0x00, 0x09), 3, "test");

    assertEquals(5, result.firstFrameOffsetDeltaBefore());
    assertEquals(8, result.firstFrameOffsetDeltaAfter());
    assertArrayEquals(bytes(0x00, 0x01, 0x48, 0x07, 0x00, 0x09), result.transformedBody());
  }

  @Test
  void sameLocalsOneStackItemFrameExpandsToExtendedWhenShiftedOffsetExceedsSixtyThree() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0x7f, 0x08, 0x00, 0x02), 1, "test");

    assertArrayEquals(
        bytes(0x00, 0x01, 0xf7, 0x00, 0x40, 0x08, 0x00, 0x02), result.transformedBody());
  }

  @Test
  void sameLocalsOneStackItemFrameExtendedShiftsOffsetDelta() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xf7, 0x00, 0x05, 0x01), 3, "test");

    assertArrayEquals(bytes(0x00, 0x01, 0xf7, 0x00, 0x08, 0x01), result.transformedBody());
  }

  @Test
  void chopFrameShiftsOffsetDelta() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xf8, 0x00, 0x05), 3, "test");

    assertArrayEquals(bytes(0x00, 0x01, 0xf8, 0x00, 0x08), result.transformedBody());
  }

  @Test
  void sameFrameExtendedShiftsOffsetDelta() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xfb, 0x00, 0x05), 3, "test");

    assertArrayEquals(bytes(0x00, 0x01, 0xfb, 0x00, 0x08), result.transformedBody());
  }

  @Test
  void appendFrameShiftsOffsetDeltaAndPreservesLocals() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xfc, 0x00, 0x05, 0x07, 0x00, 0x09), 3, "test");

    assertArrayEquals(
        bytes(0x00, 0x01, 0xfc, 0x00, 0x08, 0x07, 0x00, 0x09), result.transformedBody());
  }

  @Test
  void fullFrameShiftsOffsetDeltaAndPreservesLocalsAndStackItems() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(
            bytes(0x00, 0x01, 0xff, 0x00, 0x05, 0x00, 0x01, 0x07, 0x00, 0x09, 0x00, 0x01, 0x01),
            3,
            "test");

    assertArrayEquals(
        bytes(0x00, 0x01, 0xff, 0x00, 0x08, 0x00, 0x01, 0x07, 0x00, 0x09, 0x00, 0x01, 0x01),
        result.transformedBody());
  }

  @Test
  void laterFramesAreValidatedAndPreservedUnchanged() {
    byte[] body = concat(bytes(0x00, 0x02, 0x05), bytes(0xfb, 0x00, 0x07));

    SteelHookStackMapTableRewriteResult result = rewriter.rewrite(body, 3, "test");

    assertArrayEquals(
        concat(bytes(0x00, 0x02, 0x08), bytes(0xfb, 0x00, 0x07)), result.transformedBody());
  }

  @Test
  void malformedOrTruncatedFrameDataIsRejected() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xf7, 0x00), 3, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Malformed StackMapTable"));
  }

  @Test
  void unknownVerificationTypeTagIsRejected() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0x45, 0x09), 3, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("verification_type_info"));
  }

  @Test
  void firstFrameOffsetOverflowIsRejected() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x01, 0xfb, 0xff, 0xff), 1, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("exceeds 65535"));
  }

  @Test
  void trailingBytesAreRejected() {
    SteelHookStackMapTableRewriteResult result =
        rewriter.rewrite(bytes(0x00, 0x00, 0x01), 3, "test");

    assertEquals(SteelHookStackMapTableRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Trailing bytes"));
  }

  private byte[] bytes(int... values) {
    byte[] bytes = new byte[values.length];
    for (int index = 0; index < values.length; index++) {
      bytes[index] = (byte) values[index];
    }
    return bytes;
  }

  private byte[] concat(byte[] left, byte[] right) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(left.length + right.length);
    bytes.writeBytes(left);
    bytes.writeBytes(right);
    return bytes.toByteArray();
  }
}
