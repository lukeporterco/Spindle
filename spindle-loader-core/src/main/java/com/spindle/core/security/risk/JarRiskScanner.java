package com.spindle.core.security.risk;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class JarRiskScanner {
  private static final long MAX_CLASS_ENTRY_BYTES = 10L * 1024L * 1024L;
  private final ClassConstantPoolRiskScanner classScanner = new ClassConstantPoolRiskScanner();
  private final long maxClassEntryBytes;

  public JarRiskScanner() {
    this(MAX_CLASS_ENTRY_BYTES);
  }

  JarRiskScanner(long maxClassEntryBytes) {
    this.maxClassEntryBytes = maxClassEntryBytes;
  }

  public JarRiskScanResult scan(StaticRiskAnalyzer.TargetMod mod) throws LoaderException {
    List<ClassEntryScan> classEntries = new ArrayList<>();
    List<String> nativeEntries = new ArrayList<>();
    List<String> serviceEntries = new ArrayList<>();
    List<String> embeddedJarEntries = new ArrayList<>();
    List<StaticRiskSignal> scanWarnings = new ArrayList<>();

    try (JarFile jarFile = new JarFile(mod.jarPath().toFile())) {
      List<? extends ZipEntry> entries =
          jarFile.stream().sorted(Comparator.comparing(ZipEntry::getName)).toList();
      for (ZipEntry entry : entries) {
        if (entry.isDirectory()) {
          continue;
        }
        String entryName = normalizeEntryName(entry.getName());
        if (entryName.endsWith(".class")) {
          long entrySize = entry.getSize();
          if (entrySize > maxClassEntryBytes) {
            scanWarnings.add(oversizedClassWarning(mod, entryName, entrySize));
            continue;
          }
          byte[] classBytes;
          try (var inputStream = jarFile.getInputStream(entry)) {
            classBytes = inputStream.readAllBytes();
          }
          if (classBytes.length > maxClassEntryBytes) {
            scanWarnings.add(oversizedClassWarning(mod, entryName, classBytes.length));
            continue;
          }
          ClassConstantPoolRiskScanner.ScanResult scanResult = classScanner.scan(classBytes);
          if (scanResult.hasWarning()) {
            scanWarnings.add(
                new StaticRiskSignal(
                    StaticRiskRuleId.RISK_CLASSFILE_001,
                    StaticRiskSeverity.WARNING,
                    mod.id(),
                    SecurityLocation.of("class-entry", entryName),
                    scanResult.warningReason(),
                    "Mod `"
                        + mod.id()
                        + "` contains class entry `"
                        + entryName
                        + "` that Spindle could not statically scan. The mod may still be legitimate, but users and pack builders should review the jar build output.",
                    "Rebuild the jar with valid class files so Spindle can scan it deterministically, or remove the malformed entry if it is unused."));
            continue;
          }
          classEntries.add(new ClassEntryScan(entryName, scanResult.utf8Constants()));
          continue;
        }
        if (isNative(entryName)) {
          nativeEntries.add(entryName);
        }
        if (isServiceEntry(entryName)) {
          serviceEntries.add(entryName);
        }
        if (isEmbeddedJar(entryName)) {
          embeddedJarEntries.add(entryName);
        }
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to scan mod jar for static risk signals: " + mod.normalizedRelativePath(),
          exception);
    }

    return new JarRiskScanResult(
        mod.id(),
        mod.normalizedRelativePath(),
        List.copyOf(classEntries),
        List.copyOf(nativeEntries),
        List.copyOf(serviceEntries),
        List.copyOf(embeddedJarEntries),
        List.copyOf(scanWarnings));
  }

  private String normalizeEntryName(String entryName) {
    return entryName.replace('\\', '/');
  }

  private boolean isNative(String entryName) {
    String lower = entryName.toLowerCase(Locale.ROOT);
    return lower.endsWith(".dll")
        || lower.endsWith(".so")
        || lower.endsWith(".dylib")
        || lower.endsWith(".jnilib");
  }

  private boolean isServiceEntry(String entryName) {
    return entryName.startsWith("META-INF/services/")
        && entryName.length() > "META-INF/services/".length();
  }

  private boolean isEmbeddedJar(String entryName) {
    return entryName.toLowerCase(Locale.ROOT).endsWith(".jar");
  }

  private StaticRiskSignal oversizedClassWarning(
      StaticRiskAnalyzer.TargetMod mod, String entryName, long size) {
    return new StaticRiskSignal(
        StaticRiskRuleId.RISK_CLASSFILE_001,
        StaticRiskSeverity.WARNING,
        mod.id(),
        SecurityLocation.of("class-entry", entryName),
        "class entry size " + size + " bytes exceeded scan cap " + maxClassEntryBytes + " bytes",
        "Mod `"
            + mod.id()
            + "` contains oversized class entry `"
            + entryName
            + "`, so Spindle skipped deterministic static class scanning for that entry. Users and pack builders should review the jar build output.",
        "Reduce oversized generated class entries when possible, or review the jar manually before distribution.");
  }

  public record JarRiskScanResult(
      String modId,
      String jarPath,
      List<ClassEntryScan> classEntries,
      List<String> nativeEntries,
      List<String> serviceEntries,
      List<String> embeddedJarEntries,
      List<StaticRiskSignal> scanWarnings) {}

  public record ClassEntryScan(String entryName, List<String> utf8Constants) {
    public ClassEntryScan {
      utf8Constants = List.copyOf(utf8Constants);
    }
  }
}
