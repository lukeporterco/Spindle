package com.spindle.core.security.tool;

import com.google.gson.Gson;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.process.JavaExecutableResolver;
import com.spindle.core.process.ProcessOutputCapture;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.security.SecurityFinding;
import com.spindle.core.security.SecurityLocation;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.core.security.SecuritySeverity;
import com.spindle.core.security.risk.StaticRiskAnalyzer;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class RestrictedToolProcessLauncher {
  private static final long DEFAULT_TIMEOUT_SECONDS = 120L;
  private static final String DEFAULT_WORKER_MAIN_CLASS =
      SecurityRiskScanWorkerMain.class.getName();

  private final JavaExecutableResolver javaExecutableResolver;
  private final RestrictedToolOutputReader outputReader;
  private final RestrictedToolRequestWriter requestWriter;
  private final StaticRiskAnalyzer staticRiskAnalyzer;
  private final List<Path> workerClasspathEntries;
  private final String workerMainClassName;
  private final long timeoutSeconds;

  public RestrictedToolProcessLauncher() {
    this(
        new JavaExecutableResolver(),
        new RestrictedToolOutputReader(),
        new RestrictedToolRequestWriter(),
        new StaticRiskAnalyzer(),
        DEFAULT_WORKER_MAIN_CLASS,
        defaultWorkerClasspathEntries(),
        DEFAULT_TIMEOUT_SECONDS);
  }

  public RestrictedToolProcessLauncher(
      JavaExecutableResolver javaExecutableResolver,
      RestrictedToolOutputReader outputReader,
      RestrictedToolRequestWriter requestWriter,
      StaticRiskAnalyzer staticRiskAnalyzer,
      String workerMainClassName,
      List<Path> workerClasspathEntries,
      long timeoutSeconds) {
    this.javaExecutableResolver = javaExecutableResolver;
    this.outputReader = outputReader;
    this.requestWriter = requestWriter;
    this.staticRiskAnalyzer = staticRiskAnalyzer;
    this.workerMainClassName = workerMainClassName;
    this.workerClasspathEntries = List.copyOf(workerClasspathEntries);
    this.timeoutSeconds = timeoutSeconds;
  }

  public RestrictedToolResult runStaticRiskScan(
      Path workingDirectory, List<ResolvedModSet.ResolvedMod> mods) {
    RestrictedToolRequest request = RestrictedToolRequest.staticRiskScan(workingDirectory, mods);
    try {
      Files.createDirectories(request.outputPath().getParent());
      Files.deleteIfExists(request.outputPath());
      requestWriter.write(request.requestPath(), request);
    } catch (IOException exception) {
      return failure(
          request,
          "Failed to prepare restricted tool output path: "
              + conciseMessage(exception.getMessage()));
    } catch (LoaderException exception) {
      return failure(
          request,
          "Failed to prepare restricted tool request: " + conciseMessage(exception.getMessage()));
    }

    List<String> command;
    try {
      command = buildCommand(request);
    } catch (LoaderException exception) {
      return failure(request, conciseMessage(exception.getMessage()));
    }

    ProcessOutputCapture outputCapture = new ProcessOutputCapture(80);
    Process process;
    try {
      process = new ProcessBuilder(command).directory(request.workingDirectory().toFile()).start();
    } catch (IOException exception) {
      return failure(
          request,
          "Failed to start restricted child JVM: " + conciseMessage(exception.getMessage()));
    }

    Thread stdoutThread =
        createReaderThread(
            process.inputReader(StandardCharsets.UTF_8), outputCapture::appendStdout);
    Thread stderrThread =
        createReaderThread(
            process.errorReader(StandardCharsets.UTF_8), outputCapture::appendStderr);
    stdoutThread.start();
    stderrThread.start();

    Integer exitCode = null;
    boolean timedOut = false;
    try {
      if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        timedOut = true;
        destroyProcess(process);
      } else {
        exitCode = process.exitValue();
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      destroyProcess(process);
      return failure(request, "Interrupted while waiting for restricted child JVM.");
    } finally {
      joinQuietly(stdoutThread);
      joinQuietly(stderrThread);
    }

    if (timedOut) {
      return failure(
          request,
          "Worker `" + request.worker() + "` timed out after " + timeoutSeconds + " seconds.");
    }
    if (exitCode == null || exitCode != 0) {
      return failure(
          request,
          "Worker `"
              + request.worker()
              + "` exited with status "
              + (exitCode == null ? "unknown" : exitCode)
              + ". "
              + conciseTail(outputCapture));
    }

    try {
      RestrictedToolReport report = outputReader.read(request.outputPath(), request);
      StaticRiskAnalyzer.Analysis analysis =
          staticRiskAnalyzer.analysisFromSignals(request.targetMods(), report.staticRiskSignals());
      return new RestrictedToolResult(
          report.mode(),
          report.worker(),
          RestrictedToolResult.STATUS_PASSED,
          request.relativeOutputPath(),
          analysis,
          analysis.findings());
    } catch (LoaderException exception) {
      return failure(
          request, "Worker output validation failed: " + conciseMessage(exception.getMessage()));
    }
  }

  List<String> buildCommand(RestrictedToolRequest request) throws LoaderException {
    List<String> command = new ArrayList<>();
    command.add(javaExecutableResolver.resolve().toString());
    command.add("-cp");
    command.add(workerClasspath());
    command.add(workerMainClassName);
    command.add("--request");
    command.add(request.requestPath().toString());
    return List.copyOf(command);
  }

  private String workerClasspath() throws LoaderException {
    if (workerClasspathEntries.isEmpty()) {
      throw new LoaderException("Restricted tool worker classpath was empty.");
    }
    for (Path entry : workerClasspathEntries) {
      if (!Files.exists(entry)) {
        throw new LoaderException(
            "Restricted tool worker classpath entry was missing: "
                + entry.toString().replace('\\', '/'));
      }
    }
    return workerClasspathEntries.stream()
        .map(path -> path.toAbsolutePath().normalize().toString())
        .collect(java.util.stream.Collectors.joining(System.getProperty("path.separator")));
  }

  private RestrictedToolResult failure(RestrictedToolRequest request, String reason) {
    String outputPath = request.relativeOutputPath();
    String sanitizedReason = sanitizeForReport(reason, request);
    SecurityFinding finding =
        new SecurityFinding(
            SecurityRuleId.SEC_TOOL_001,
            SecuritySeverity.FATAL,
            null,
            SecurityLocation.of("tool-output", outputPath),
            "Restricted security tool `"
                + request.worker()
                + "` failed in `"
                + RestrictedToolExecutionMode.RESTRICTED_CHILD_JVM.id()
                + "` mode. "
                + conciseMessage(sanitizedReason)
                + " Output path: `"
                + outputPath
                + "`.",
            "Rerun Spindle, inspect diagnostics, clear `.spindle/security-tools`, or report a Spindle bug if the failure is reproducible.");
    return new RestrictedToolResult(
        RestrictedToolExecutionMode.RESTRICTED_CHILD_JVM,
        request.worker(),
        RestrictedToolResult.STATUS_FAILED,
        outputPath,
        StaticRiskAnalyzer.Analysis.EMPTY,
        List.of(finding));
  }

  private String conciseTail(ProcessOutputCapture outputCapture) {
    String stderr = lastNonBlankLine(outputCapture.stderrTail());
    if (stderr != null) {
      return "Worker stderr: " + conciseMessage(stderr);
    }
    String stdout = lastNonBlankLine(outputCapture.stdoutTail());
    if (stdout != null) {
      return "Worker stdout: " + conciseMessage(stdout);
    }
    return "The worker did not provide additional diagnostics.";
  }

  private String lastNonBlankLine(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    String[] lines = text.split("\\R");
    for (int index = lines.length - 1; index >= 0; index--) {
      String line = lines[index].trim();
      if (!line.isEmpty()) {
        return line;
      }
    }
    return null;
  }

  private String conciseMessage(String text) {
    if (text == null) {
      return "No additional details were provided.";
    }
    String normalized = text.replace('\r', ' ').replace('\n', ' ').trim();
    if (normalized.isEmpty()) {
      return "No additional details were provided.";
    }
    return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
  }

  private String sanitizeForReport(String text, RestrictedToolRequest request) {
    if (text == null) {
      return null;
    }
    String sanitized = text.replace('\\', '/');
    sanitized =
        sanitized.replace(
            request.workingDirectory().toString().replace('\\', '/'), "[absolute path]");
    sanitized =
        sanitized.replace(
            request.outputPath().toString().replace('\\', '/'), request.relativeOutputPath());
    for (RestrictedToolRequest.ModInput mod : request.mods()) {
      sanitized = sanitized.replace(mod.jarPath().toString().replace('\\', '/'), "[absolute path]");
    }
    return sanitized;
  }

  private Thread createReaderThread(
      BufferedReader reader, java.util.function.Consumer<String> sink) {
    return new Thread(
        () -> {
          try (reader) {
            String line;
            while ((line = reader.readLine()) != null) {
              sink.accept(line);
            }
          } catch (IOException ignored) {
          }
        },
        "restricted-tool-output");
  }

  private void destroyProcess(Process process) {
    if (!process.isAlive()) {
      return;
    }
    process.destroy();
    try {
      if (!process.waitFor(5, TimeUnit.SECONDS) && process.isAlive()) {
        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
    }
  }

  private void joinQuietly(Thread thread) {
    try {
      thread.join(1_000L);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }

  private static List<Path> defaultWorkerClasspathEntries() {
    Set<Path> entries = new LinkedHashSet<>();
    entries.add(classpathEntryFor(SecurityRiskScanWorkerMain.class));
    entries.add(classpathEntryFor(Gson.class));
    return List.copyOf(entries);
  }

  private static Path classpathEntryFor(Class<?> type) {
    try {
      return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI())
          .toAbsolutePath()
          .normalize();
    } catch (URISyntaxException exception) {
      throw new IllegalStateException(
          "Failed to resolve classpath entry for " + type.getName(), exception);
    }
  }
}
