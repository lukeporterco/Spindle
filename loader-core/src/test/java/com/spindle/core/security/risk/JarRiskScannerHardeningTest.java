package com.spindle.core.security.risk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JarRiskScannerHardeningTest {
  @TempDir Path tempDirectory;

  @Test
  void staticRiskScannerWarnsAndSkipsOversizedClassEntry() throws Exception {
    Path jarPath = tempDirectory.resolve("scanner/oversized.jar");
    createJar(
        jarPath,
        Map.of(
            "com/example/risk/Oversized.class",
            new byte[] {1, 2, 3, 4, 5, 6},
            "com/example/risk/Normal.class",
            minimalClassBytes("java/lang/ProcessBuilder")));

    JarRiskScanner.JarRiskScanResult result =
        new JarRiskScanner(4L)
            .scan(new StaticRiskAnalyzer.TargetMod("oversizedmod", "mods/oversized.jar", jarPath));

    assertTrue(
        result.scanWarnings().stream()
            .anyMatch(signal -> signal.ruleId() == StaticRiskRuleId.RISK_CLASSFILE_001));
    assertTrue(
        result.classEntries().stream()
            .noneMatch(entry -> entry.entryName().equals("com/example/risk/Oversized.class")));
  }

  private void createJar(Path jarPath, Map<String, byte[]> entries) throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    }
  }

  private byte[] minimalClassBytes(String... utf8Values) throws IOException {
    TreeSet<String> values = new TreeSet<>(java.util.List.of(utf8Values));
    java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
    try (java.io.DataOutputStream data = new java.io.DataOutputStream(outputStream)) {
      data.writeInt(0xCAFEBABE);
      data.writeShort(0);
      data.writeShort(69);
      data.writeShort(values.size() + 1);
      for (String value : values) {
        data.writeByte(1);
        data.writeUTF(value);
      }
      data.writeShort(0x0021);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
      data.writeShort(0);
    }
    return outputStream.toByteArray();
  }
}
