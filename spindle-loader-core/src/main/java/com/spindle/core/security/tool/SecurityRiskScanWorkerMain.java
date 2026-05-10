package com.spindle.core.security.tool;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.risk.StaticRiskAnalyzer;
import java.nio.file.Path;

public final class SecurityRiskScanWorkerMain {
  private final StaticRiskAnalyzer staticRiskAnalyzer = new StaticRiskAnalyzer();
  private final RestrictedToolReportWriter reportWriter = new RestrictedToolReportWriter();
  private final RestrictedToolRequestReader requestReader = new RestrictedToolRequestReader();

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
    Path requestPath = null;
    for (int index = 0; index < args.length; index++) {
      String arg = args[index];
      if (index + 1 >= args.length) {
        throw new IllegalArgumentException("Missing value for " + arg);
      }
      String value = args[++index];
      switch (arg) {
        case "--request" -> requestPath = Path.of(value);
        default -> throw new IllegalArgumentException("Unknown argument `" + arg + "`.");
      }
    }
    if (requestPath == null) {
      throw new IllegalArgumentException("Missing required `--request`.");
    }
    return new WorkerArguments(requestReader.read(requestPath));
  }

  private record WorkerArguments(RestrictedToolRequest request) {}
}
