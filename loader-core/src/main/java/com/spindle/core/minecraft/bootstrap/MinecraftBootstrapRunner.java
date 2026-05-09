package com.spindle.core.minecraft.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.api.minecraft.MinecraftServerModInitializer;
import com.spindle.core.LoaderMain;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraph;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraphBuilder;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraphWriter;
import com.spindle.core.minecraft.MinecraftClassLoaderPolicy;
import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.MinecraftModClassLoader;
import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftModExecutionResult;
import com.spindle.core.minecraft.MinecraftModExecutionResultWriter;
import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import com.spindle.core.minecraft.MinecraftProtectedPackagePolicy;
import com.spindle.core.minecraft.MinecraftRuntimeClassLoader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class MinecraftBootstrapRunner {
  private final Gson gson = new Gson();

  public MinecraftBootstrapResult run(MinecraftBootstrapArguments arguments)
      throws LoaderException {
    Path workingDirectory = arguments.workingDirectory();
    Path graphPath = workingDirectory.resolve("minecraft-bootstrap-classloader-graph.json");
    Path executionResultPath = workingDirectory.resolve("minecraft-mod-execution-result.json");
    Path bootstrapResultPath = workingDirectory.resolve("minecraft-server-bootstrap-result.json");
    Path driftReportPath = workingDirectory.resolve("minecraft-plan-drift-report.json");
    MinecraftBootstrapPlanVerifier.VerifiedPlans verifiedPlans;
    try {
      verifiedPlans = new MinecraftBootstrapPlanVerifier().verify(arguments);
    } catch (MinecraftBootstrapPlanVerifier.PlanDriftException exception) {
      writeDriftReport(driftReportPath, exception.failure());
      MinecraftBootstrapResult result =
          failureResult(
              arguments,
              null,
              null,
              null,
              null,
              MinecraftBootstrapExitCode.PLAN_DRIFT,
              exception.failure());
      new MinecraftBootstrapResultWriter().write(bootstrapResultPath, result);
      return result;
    }

    MinecraftModExecutionPlan executionPlan =
        gson.fromJson(verifiedPlans.executionPlan(), MinecraftModExecutionPlan.class);
    MinecraftBootstrapClassLoaderGraph graph =
        new MinecraftBootstrapClassLoaderGraphBuilder().build(executionPlan);
    new MinecraftBootstrapClassLoaderGraphWriter().write(graphPath, graph);
    MinecraftPlanFingerprint graphFingerprint =
        MinecraftPlanFingerprint.fromFile("classloader-graph", graphPath);
    MinecraftClassLoadingAudit audit = new MinecraftClassLoadingAudit();
    MinecraftProtectedPackagePolicy protectedPackagePolicy = new MinecraftProtectedPackagePolicy();
    MinecraftClassLoaderPolicy classLoaderPolicy = executionPlan.classLoaderPolicy();

    List<URL> runtimeUrls = runtimeUrls(arguments.workingDirectory(), verifiedPlans.runtimePlan());
    List<String> entrypointsAttempted = new ArrayList<>();
    List<String> entrypointsSucceeded = new ArrayList<>();
    List<String> entrypointsFailed = new ArrayList<>();
    List<String> entrypointInvocationOrder = new ArrayList<>();
    List<String> failureReasons = new ArrayList<>();
    Map<String, String> markerOutputs = new TreeMap<>();
    boolean minecraftMainInvoked = false;
    Integer processOutcome = null;

    try (MinecraftRuntimeClassLoader runtimeClassLoader =
        new MinecraftRuntimeClassLoader(
            "minecraft-runtime",
            runtimeUrls.toArray(URL[]::new),
            LoaderMain.class.getClassLoader(),
            audit)) {
      for (var executableMod :
          executionPlan.acceptedExecutableMods().stream()
              .sorted(Comparator.comparing(mod -> mod.modId()))
              .toList()) {
        Path modJar = resolve(arguments.workingDirectory(), executableMod.modJarPath());
        try (MinecraftModClassLoader modClassLoader =
            new MinecraftModClassLoader(
                executableMod.plannedModClassLoaderId(),
                new URL[] {modJar.toUri().toURL()},
                runtimeClassLoader,
                classLoaderPolicy,
                protectedPackagePolicy,
                audit)) {
          for (var declaration : executableMod.entrypoints()) {
            String qualifiedEntrypoint = executableMod.modId() + ":" + declaration.className();
            entrypointsAttempted.add(qualifiedEntrypoint);
            entrypointInvocationOrder.add(qualifiedEntrypoint);
            try {
              Class<?> entrypointClass =
                  Class.forName(declaration.className(), true, modClassLoader);
              if (!MinecraftServerModInitializer.class.isAssignableFrom(entrypointClass)) {
                throw new LoaderException(
                    "Entrypoint does not implement MinecraftServerModInitializer: "
                        + declaration.className());
              }
              MinecraftServerModInitializer initializer =
                  MinecraftServerModInitializer.class.cast(
                      entrypointClass.getDeclaredConstructor().newInstance());
              Path gameDirectory = workingDirectory.resolve("game");
              Path configDirectory = workingDirectory.resolve("config");
              Path modDataDirectory =
                  workingDirectory.resolve("mod-data").resolve(executableMod.modId());
              Files.createDirectories(gameDirectory);
              Files.createDirectories(configDirectory);
              Files.createDirectories(modDataDirectory);
              initializer.onInitializeMinecraftServer(
                  new DefaultMinecraftServerModContext(
                      executableMod.modId(),
                      executableMod.version(),
                      executionPlan.resolvedMinecraftVersion(),
                      LoaderMain.LOADER_VERSION,
                      executionPlan.side(),
                      gameDirectory,
                      configDirectory,
                      modDataDirectory));
              entrypointsSucceeded.add(qualifiedEntrypoint);
              markerOutputs.putAll(readMarkerOutputs(workingDirectory, executableMod.modId()));
            } catch (InvocationTargetException exception) {
              entrypointsFailed.add(qualifiedEntrypoint);
              failureReasons.add(
                  "entrypoint failure "
                      + qualifiedEntrypoint
                      + ": "
                      + exception.getTargetException().getMessage());
            } catch (ReflectiveOperationException | IOException exception) {
              entrypointsFailed.add(qualifiedEntrypoint);
              failureReasons.add(
                  "entrypoint failure " + qualifiedEntrypoint + ": " + exception.getMessage());
            } catch (Exception exception) {
              entrypointsFailed.add(qualifiedEntrypoint);
              failureReasons.add(
                  "entrypoint failure " + qualifiedEntrypoint + ": " + exception.getMessage());
            }
          }
        }
      }

      if (entrypointsFailed.isEmpty()) {
        try {
          Class<?> mainClass =
              Class.forName(executionPlan.minecraftMainClass(), true, runtimeClassLoader);
          mainClass
              .getMethod("main", String[].class)
              .invoke(null, (Object) executionPlan.minecraftMainArgs().toArray(String[]::new));
          minecraftMainInvoked = true;
          processOutcome = 0;
        } catch (InvocationTargetException exception) {
          failureReasons.add(
              "minecraft main failure: " + exception.getTargetException().getMessage());
          processOutcome = 1;
        } catch (ReflectiveOperationException exception) {
          failureReasons.add("minecraft main failure: " + exception.getMessage());
          processOutcome = 1;
        }
      }
    } catch (IOException exception) {
      throw new LoaderException("Failed to close bootstrap classloaders", exception);
    }

    MinecraftModExecutionResult executionResult =
        new MinecraftModExecutionResult(
            1,
            "Milestone 8",
            "bootstrap",
            executionPlan.resolvedMinecraftVersion(),
            executionPlan.acceptedExecutableMods().stream()
                .map(mod -> mod.modId())
                .sorted()
                .toList(),
            entrypointsAttempted,
            entrypointsSucceeded,
            entrypointsFailed,
            entrypointInvocationOrder,
            failureReasons,
            markerOutputs,
            audit.summary(),
            executionPlan.proof(),
            minecraftMainInvoked,
            executionPlan.minecraftMainClass(),
            executionPlan.minecraftMainArgs(),
            processOutcome);
    new MinecraftModExecutionResultWriter().write(executionResultPath, executionResult);
    MinecraftPlanFingerprint executionResultFingerprint =
        MinecraftPlanFingerprint.fromFile("mod-execution-result", executionResultPath);
    MinecraftBootstrapFailure failure =
        failureReasons.isEmpty()
            ? null
            : new MinecraftBootstrapFailure(
                entrypointsFailed.isEmpty() ? "minecraft-main-failure" : "mod-entrypoint-failure",
                failureReasons.getFirst(),
                failureReasons);
    MinecraftBootstrapResult bootstrapResult =
        failureResult(
            arguments,
            verifiedPlans.runtimeFingerprint(),
            verifiedPlans.executionFingerprint(),
            verifiedPlans.integrationFingerprint(),
            graphFingerprint,
            failure == null
                ? MinecraftBootstrapExitCode.SUCCESS
                : (entrypointsFailed.isEmpty()
                    ? MinecraftBootstrapExitCode.MINECRAFT_MAIN_FAILURE
                    : MinecraftBootstrapExitCode.MOD_ENTRYPOINT_FAILURE),
            failure,
            executionResultFingerprint,
            minecraftMainInvoked);
    new MinecraftBootstrapResultWriter().write(bootstrapResultPath, bootstrapResult);
    return bootstrapResult;
  }

  private List<URL> runtimeUrls(Path workingDirectory, JsonObject runtimePlan)
      throws LoaderException {
    List<URL> urls = new ArrayList<>();
    urls.add(toUrl(resolve(workingDirectory, runtimePlan.get("serverJarPath").getAsString())));
    JsonArray classpathEntries = runtimePlan.getAsJsonArray("classpathEntries");
    if (classpathEntries != null) {
      for (int index = 0; index < classpathEntries.size(); index++) {
        urls.add(
            toUrl(
                resolve(
                    workingDirectory,
                    classpathEntries.get(index).getAsJsonObject().get("path").getAsString())));
      }
    }
    return urls;
  }

  private URL toUrl(Path path) throws LoaderException {
    try {
      return path.toUri().toURL();
    } catch (Exception exception) {
      throw new LoaderException(
          "Failed to build bootstrap classpath URL for " + path.toString().replace('\\', '/'),
          exception);
    }
  }

  private Path resolve(Path workingDirectory, String serializedPath) {
    Path path = Path.of(serializedPath);
    return path.isAbsolute()
        ? path.normalize()
        : workingDirectory.resolve(path).toAbsolutePath().normalize();
  }

  private Map<String, String> readMarkerOutputs(Path workingDirectory, String modId)
      throws IOException {
    Map<String, String> outputs = new TreeMap<>();
    Path modDataDirectory = workingDirectory.resolve("mod-data").resolve(modId);
    if (!Files.isDirectory(modDataDirectory)) {
      return outputs;
    }
    try (var stream = Files.walk(modDataDirectory)) {
      for (Path path : stream.filter(Files::isRegularFile).sorted().toList()) {
        String relative =
            workingDirectory
                .relativize(path.toAbsolutePath().normalize())
                .toString()
                .replace('\\', '/');
        outputs.put(relative, Files.readString(path, StandardCharsets.UTF_8));
      }
    }
    return outputs;
  }

  private void writeDriftReport(Path path, MinecraftBootstrapFailure failure)
      throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Milestone 8");
    root.addProperty("failureCategory", failure.category());
    root.addProperty("failureMessage", failure.message());
    JsonArray details = new JsonArray();
    failure.details().forEach(details::add);
    root.add("details", details);
    try {
      Files.writeString(path, new Gson().toJson(root), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft bootstrap drift report " + path.toString().replace('\\', '/'),
          exception);
    }
  }

  private MinecraftBootstrapResult failureResult(
      MinecraftBootstrapArguments arguments,
      MinecraftPlanFingerprint runtimeFingerprint,
      MinecraftPlanFingerprint executionFingerprint,
      MinecraftPlanFingerprint integrationFingerprint,
      MinecraftPlanFingerprint graphFingerprint,
      MinecraftBootstrapExitCode exitCode,
      MinecraftBootstrapFailure failure) {
    return failureResult(
        arguments,
        runtimeFingerprint,
        executionFingerprint,
        integrationFingerprint,
        graphFingerprint,
        exitCode,
        failure,
        null,
        false);
  }

  private MinecraftBootstrapResult failureResult(
      MinecraftBootstrapArguments arguments,
      MinecraftPlanFingerprint runtimeFingerprint,
      MinecraftPlanFingerprint executionFingerprint,
      MinecraftPlanFingerprint integrationFingerprint,
      MinecraftPlanFingerprint graphFingerprint,
      MinecraftBootstrapExitCode exitCode,
      MinecraftBootstrapFailure failure,
      MinecraftPlanFingerprint modExecutionResultFingerprint,
      boolean minecraftMainInvoked) {
    return new MinecraftBootstrapResult(
        1,
        "Milestone 8",
        List.of(
            "--verify-plan-fingerprints=" + arguments.verifyPlanFingerprints(),
            "--strict-execution=" + arguments.strictExecution(),
            "--offline-bootstrap=" + arguments.offlineBootstrap()),
        System.getProperty("java.home", "").replace('\\', '/') + "/bin/java",
        runtimeFingerprint,
        executionFingerprint,
        integrationFingerprint,
        graphFingerprint,
        modExecutionResultFingerprint,
        minecraftMainInvoked,
        exitCode.code(),
        failure == null ? null : failure.category(),
        failure == null ? null : failure.message(),
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }
}
