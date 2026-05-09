package com.spindle.core.security.risk;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.security.SecurityFinding;
import com.spindle.core.security.SecurityLocation;
import com.spindle.core.security.SecuritySeverity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class StaticRiskAnalyzer {
  private static final List<String> NETWORK_PREFIXES =
      List.of("java/net/", "java/net/http/", "javax/net/");
  private static final List<String> SCRIPT_PREFIXES = List.of("javax/script/", "jdk/nashorn/");
  private static final List<String> UNSAFE_PREFIXES =
      List.of("jdk/internal/", "java/lang/foreign/");

  private final JarRiskScanner jarRiskScanner = new JarRiskScanner();

  public Analysis analyze(List<ResolvedModSet.ResolvedMod> mods) throws LoaderException {
    List<StaticRiskSignal> signals = new ArrayList<>();
    Map<String, String> jarPathsByModId = new java.util.TreeMap<>();
    for (ResolvedModSet.ResolvedMod mod :
        mods.stream()
            .sorted(Comparator.comparing(ResolvedModSet.ResolvedMod::normalizedRelativePath))
            .toList()) {
      jarPathsByModId.put(mod.id(), mod.normalizedRelativePath());
      analyzeMod(mod, signals);
    }
    List<StaticRiskSignal> sortedSignals = signals.stream().sorted(StaticRiskSignal.ORDER).toList();
    return new Analysis(
        StaticRiskSummary.from(sortedSignals),
        sortedSignals,
        summarizeFindings(sortedSignals, jarPathsByModId));
  }

  private void analyzeMod(ResolvedModSet.ResolvedMod mod, List<StaticRiskSignal> signals)
      throws LoaderException {
    JarRiskScanner.JarRiskScanResult scanResult = jarRiskScanner.scan(mod);
    signals.addAll(scanResult.scanWarnings());

    for (JarRiskScanner.ClassEntryScan classEntry : scanResult.classEntries()) {
      List<String> utf8Constants = classEntry.utf8Constants();
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_PROCESS_001,
          processEvidence(utf8Constants),
          "Mod `"
              + mod.id()
              + "` references process execution APIs. This can be legitimate, but users and pack builders should review why the mod needs it.",
          "Remove unused process-launching code when possible, or document why the mod needs to start child processes.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_NATIVE_001,
          nativeApiEvidence(utf8Constants),
          "Mod `"
              + mod.id()
              + "` references native library loading APIs. This can be legitimate, but users and pack builders should review why the mod needs native code.",
          "Avoid native loading when possible, or document why the mod needs native code and which platforms it targets.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_NETWORK_001,
          prefixEvidence(utf8Constants, NETWORK_PREFIXES),
          "Mod `"
              + mod.id()
              + "` references network APIs. This can be legitimate, but users and pack builders should review why the mod needs network access.",
          "Remove unused networking dependencies when possible, or document the expected outbound or inbound network behavior.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_REFLECTION_001,
          reflectionEvidence(utf8Constants),
          "Mod `"
              + mod.id()
              + "` references reflection or method-handle APIs. This can be legitimate, but users and pack builders should review why the mod needs reflective access.",
          "Remove unused reflective access when possible, or document why the mod needs reflection or method-handle behavior.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_UNSAFE_001,
          unsafeEvidence(utf8Constants),
          "Mod `"
              + mod.id()
              + "` references unsafe, internal, or foreign-memory APIs. This can be legitimate, but users and pack builders should review the compatibility and maintenance risk.",
          "Prefer stable public APIs when possible, or document why the mod depends on internal or low-level runtime access.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_DYNAMIC_CLASSLOAD_001,
          dynamicClassloadEvidence(utf8Constants),
          "Mod `"
              + mod.id()
              + "` references dynamic classloading APIs. This can be legitimate, but users and pack builders should review why the mod needs to define or load classes dynamically.",
          "Avoid dynamic classloading when possible, or document why the mod needs custom class definition or classpath behavior.");
      addSignalIfPresent(
          signals,
          mod.id(),
          classEntry.entryName(),
          StaticRiskRuleId.RISK_SCRIPT_001,
          prefixEvidence(utf8Constants, SCRIPT_PREFIXES),
          "Mod `"
              + mod.id()
              + "` references script execution APIs. This can be legitimate, but users and pack builders should review why the mod needs dynamic scripting.",
          "Remove unused scripting support when possible, or document the script engine and intended behavior.");
    }

    for (String entryName : scanResult.nativeEntries()) {
      signals.add(
          new StaticRiskSignal(
              StaticRiskRuleId.RISK_NATIVE_001,
              StaticRiskSeverity.WARNING,
              mod.id(),
              SecurityLocation.of("jar-entry", entryName),
              nativeEvidence(entryName),
              "Mod `"
                  + mod.id()
                  + "` bundles a native library file. This is a stronger warning because native code sits outside the usual Java boundary and users should review why it is present.",
              "Avoid bundling native libraries when possible, or document the native component, supported platforms, and expected behavior."));
    }
    for (String entryName : scanResult.serviceEntries()) {
      signals.add(
          new StaticRiskSignal(
              StaticRiskRuleId.RISK_SERVICE_001,
              StaticRiskSeverity.WARNING,
              mod.id(),
              SecurityLocation.of("jar-entry", entryName),
              "service provider file",
              "Mod `"
                  + mod.id()
                  + "` declares a service-provider entry. This can be legitimate, but users and pack builders should review which service extension point it targets.",
              "Remove unused service-provider files when possible, or document which SPI the mod implements and why."));
    }
    for (String entryName : scanResult.embeddedJarEntries()) {
      signals.add(
          new StaticRiskSignal(
              StaticRiskRuleId.RISK_EMBEDDED_JAR_001,
              StaticRiskSeverity.WARNING,
              mod.id(),
              SecurityLocation.of("jar-entry", entryName),
              "nested jar entry",
              "Mod `"
                  + mod.id()
                  + "` bundles a nested jar file. This can be legitimate, but users and pack builders should review why the jar contains another executable payload.",
              "Avoid bundling nested jars when possible, or document what the embedded payload is used for."));
    }
  }

  private void addSignalIfPresent(
      List<StaticRiskSignal> signals,
      String modId,
      String classEntryName,
      StaticRiskRuleId ruleId,
      List<String> evidence,
      String message,
      String fix) {
    if (evidence.isEmpty()) {
      return;
    }
    signals.add(
        new StaticRiskSignal(
            ruleId,
            StaticRiskSeverity.WARNING,
            modId,
            SecurityLocation.of("class-entry", classEntryName),
            String.join(", ", evidence),
            message,
            fix));
  }

  private List<String> processEvidence(List<String> utf8Constants) {
    TreeSet<String> evidence = new TreeSet<>();
    if (utf8Constants.contains("java/lang/ProcessBuilder")) {
      evidence.add("java/lang/ProcessBuilder");
    }
    if (utf8Constants.contains("java/lang/Runtime") && utf8Constants.contains("exec")) {
      evidence.add("java/lang/Runtime");
      evidence.add("exec");
    }
    return List.copyOf(evidence);
  }

  private List<String> nativeApiEvidence(List<String> utf8Constants) {
    TreeSet<String> evidence = new TreeSet<>();
    if (utf8Constants.contains("java/lang/System") && utf8Constants.contains("load")) {
      evidence.add("java/lang/System");
      evidence.add("load");
    }
    if (utf8Constants.contains("java/lang/System") && utf8Constants.contains("loadLibrary")) {
      evidence.add("java/lang/System");
      evidence.add("loadLibrary");
    }
    return List.copyOf(evidence);
  }

  private List<String> prefixEvidence(List<String> utf8Constants, List<String> prefixes) {
    TreeSet<String> evidence = new TreeSet<>();
    for (String constant : utf8Constants) {
      for (String prefix : prefixes) {
        if (constant.startsWith(prefix)) {
          evidence.add(constant);
          break;
        }
      }
    }
    return List.copyOf(evidence);
  }

  private List<String> reflectionEvidence(List<String> utf8Constants) {
    TreeSet<String> evidence = new TreeSet<>();
    for (String constant : utf8Constants) {
      if (constant.startsWith("java/lang/reflect/")
          || constant.startsWith("java/lang/invoke/MethodHandle")
          || constant.startsWith("java/lang/invoke/MethodHandles")
          || constant.equals("java/lang/invoke/MethodType")
          || constant.equals("java/lang/invoke/VarHandle")
          || constant.equals("java/lang/invoke/CallSite")
          || constant.equals("java/lang/invoke/MutableCallSite")
          || constant.equals("java/lang/invoke/ConstantCallSite")
          || constant.equals("java/lang/invoke/VolatileCallSite")
          || constant.equals("java/lang/invoke/SwitchPoint")) {
        evidence.add(constant);
      }
    }
    return List.copyOf(evidence);
  }

  private List<String> unsafeEvidence(List<String> utf8Constants) {
    TreeSet<String> evidence = new TreeSet<>();
    for (String constant : utf8Constants) {
      if (constant.equals("sun/misc/Unsafe")) {
        evidence.add(constant);
        continue;
      }
      for (String prefix : UNSAFE_PREFIXES) {
        if (constant.startsWith(prefix)) {
          evidence.add(constant);
        }
      }
    }
    return List.copyOf(evidence);
  }

  private List<String> dynamicClassloadEvidence(List<String> utf8Constants) {
    TreeSet<String> evidence = new TreeSet<>();
    if (utf8Constants.contains("java/lang/ClassLoader")) {
      evidence.add("java/lang/ClassLoader");
    }
    if (utf8Constants.contains("java/net/URLClassLoader")) {
      evidence.add("java/net/URLClassLoader");
    }
    if (utf8Constants.contains("defineClass")) {
      evidence.add("defineClass");
    }
    return List.copyOf(evidence);
  }

  private String nativeEvidence(String entryName) {
    int dot = entryName.lastIndexOf('.');
    return dot >= 0 ? entryName.substring(dot) : entryName;
  }

  private List<SecurityFinding> summarizeFindings(
      List<StaticRiskSignal> signals, Map<String, String> jarPathsByModId) {
    record Key(StaticRiskRuleId ruleId, String modId) {}

    Map<Key, List<StaticRiskSignal>> grouped =
        new java.util.TreeMap<>(
            Comparator.comparing((Key key) -> key.ruleId().id()).thenComparing(Key::modId));
    for (StaticRiskSignal signal : signals) {
      grouped
          .computeIfAbsent(new Key(signal.ruleId(), signal.modId()), ignored -> new ArrayList<>())
          .add(signal);
    }

    List<SecurityFinding> findings = new ArrayList<>();
    for (Map.Entry<Key, List<StaticRiskSignal>> entry : grouped.entrySet()) {
      Key key = entry.getKey();
      List<StaticRiskSignal> groupedSignals = entry.getValue();
      String jarPath = jarPathsByModId.get(key.modId());
      findings.add(
          new SecurityFinding(
              key.ruleId(),
              SecuritySeverity.WARNING,
              key.modId(),
              SecurityLocation.of("artifact", jarPath),
              summaryMessage(key.ruleId(), key.modId(), groupedSignals.size()),
              summaryFix(key.ruleId())));
    }
    return List.copyOf(findings);
  }

  private String summaryMessage(StaticRiskRuleId ruleId, String modId, int signalCount) {
    String countText =
        signalCount == 1 ? "1 static risk signal" : signalCount + " static risk signals";
    return switch (ruleId) {
      case RISK_PROCESS_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for process execution APIs. This can be legitimate, but users and pack builders should review why the mod needs it.";
      case RISK_NATIVE_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for native loading or bundled native files. This is a stronger warning because native code sits outside the usual Java boundary.";
      case RISK_NETWORK_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for network APIs. This can be legitimate, but users and pack builders should review why the mod needs network access.";
      case RISK_REFLECTION_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for reflection or method-handle APIs. This can be legitimate, but users and pack builders should review why the mod needs reflective access.";
      case RISK_UNSAFE_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for unsafe, internal, or foreign-memory APIs. This can be legitimate, but users and pack builders should review the compatibility risk.";
      case RISK_DYNAMIC_CLASSLOAD_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for dynamic classloading APIs. This can be legitimate, but users and pack builders should review why the mod needs dynamic loading.";
      case RISK_SCRIPT_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for script execution APIs. This can be legitimate, but users and pack builders should review why the mod needs dynamic scripting.";
      case RISK_SERVICE_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for service-provider entries. This can be legitimate, but users and pack builders should review which extension point it targets.";
      case RISK_EMBEDDED_JAR_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " for nested jar entries. This can be legitimate, but users and pack builders should review why the mod bundles another executable payload.";
      case RISK_CLASSFILE_001 ->
          "Mod `"
              + modId
              + "` produced "
              + countText
              + " because some class entries could not be statically scanned. The mod may still be legitimate, but users and pack builders should review the jar build output.";
    };
  }

  private String summaryFix(StaticRiskRuleId ruleId) {
    return switch (ruleId) {
      case RISK_PROCESS_001 ->
          "Remove unused process-launching code when possible, or document why the mod needs child processes.";
      case RISK_NATIVE_001 ->
          "Avoid native loading or native bundling when possible, or document why native code is required.";
      case RISK_NETWORK_001 ->
          "Remove unused networking dependencies when possible, or document the expected network behavior.";
      case RISK_REFLECTION_001 ->
          "Remove unused reflective access when possible, or document why the mod needs reflection or method handles.";
      case RISK_UNSAFE_001 ->
          "Prefer stable public APIs when possible, or document why the mod depends on internal or low-level runtime access.";
      case RISK_DYNAMIC_CLASSLOAD_001 ->
          "Avoid dynamic classloading when possible, or document why the mod needs custom loading behavior.";
      case RISK_SCRIPT_001 ->
          "Remove unused scripting support when possible, or document the script engine and intended behavior.";
      case RISK_SERVICE_001 ->
          "Remove unused service-provider files when possible, or document the SPI the mod implements.";
      case RISK_EMBEDDED_JAR_001 ->
          "Avoid bundling nested jars when possible, or document what the embedded payload is used for.";
      case RISK_CLASSFILE_001 ->
          "Rebuild the jar with valid class files so Spindle can scan it deterministically.";
    };
  }

  public record Analysis(
      StaticRiskSummary summary, List<StaticRiskSignal> signals, List<SecurityFinding> findings) {
    public Analysis {
      summary = summary == null ? StaticRiskSummary.EMPTY : summary;
      signals = List.copyOf(signals);
      findings = List.copyOf(findings);
    }
  }
}
