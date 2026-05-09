package com.spindle.core.security.tool;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.risk.StaticRiskAnalyzer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SecurityRiskScanWorkerMain {
  private final StaticRiskAnalyzer staticRiskAnalyzer = new StaticRiskAnalyzer();
  private final RestrictedToolReportWriter reportWriter = new RestrictedToolReportWriter();

  public static void main(String[] args) {
    int exitCode = new SecurityRiskScanWorkerMain().run(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  public int run(String[] args) {
    try {
      WorkerArguments workerArguments = parseArgs(args);
      StaticRiskAnalyzer.Analysis analysis =
          staticRiskAnalyzer.analyzeTargets(workerArguments.request().targetMods());
      reportWriter.write(
          workerArguments.request().outputPath(),
          new RestrictedToolReport(
              RestrictedToolReport.SCHEMA_VERSION,
              RestrictedToolReport.REPORT_KIND,
              workerArguments.request().worker(),
              RestrictedToolExecutionMode.RESTRICTED_CHILD_JVM,
              analysis.summary(),
              analysis.signals()));
      return 0;
    } catch (IllegalArgumentException exception) {
      System.err.println("Invalid restricted tool arguments: " + exception.getMessage());
      return 2;
    } catch (LoaderException exception) {
      System.err.println("Restricted static risk scan failed: " + exception.getMessage());
      return 3;
    }
  }

  private WorkerArguments parseArgs(String[] args) {
    Path workingDirectory = null;
    Path outputPath = null;
    List<RestrictedToolRequest.ModInput> mods = new ArrayList<>();
    for (int index = 0; index < args.length; index++) {
      String arg = args[index];
      if (index + 1 >= args.length) {
        throw new IllegalArgumentException("Missing value for " + arg);
      }
      String value = args[++index];
      switch (arg) {
        case "--working-directory" -> workingDirectory = Path.of(value);
        case "--output" -> outputPath = Path.of(value);
        case "--mod" -> mods.add(parseMod(value));
        default -> throw new IllegalArgumentException("Unknown argument `" + arg + "`.");
      }
    }
    if (workingDirectory == null) {
      throw new IllegalArgumentException("Missing required `--working-directory`.");
    }
    if (outputPath == null) {
      throw new IllegalArgumentException("Missing required `--output`.");
    }
    if (mods.isEmpty()) {
      throw new IllegalArgumentException("At least one `--mod` input is required.");
    }
    return new WorkerArguments(
        new RestrictedToolRequest(
            RestrictedToolRequest.STATIC_RISK_SCAN_WORKER, workingDirectory, outputPath, mods));
  }

  private RestrictedToolRequest.ModInput parseMod(String value) {
    String[] parts = value.split("\\|", 4);
    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid `--mod` value `" + value + "`.");
    }
    return new RestrictedToolRequest.ModInput(parts[0], parts[1], parts[2], Path.of(parts[3]));
  }

  private record WorkerArguments(RestrictedToolRequest request) {}
}
