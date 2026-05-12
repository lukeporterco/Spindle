package com.spindle.core.minecraft.bootstrap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class MinecraftBootstrapPlanVerifier {
  public VerifiedPlans verify(MinecraftBootstrapArguments arguments) throws LoaderException {
    JsonObject runtimePlan = read(arguments.runtimePlanPath());
    JsonObject boundaryPlan = read(arguments.boundaryPath());
    JsonObject integrationPlan = read(arguments.integrationPlanPath());
    JsonObject executionPlan = read(arguments.executionPlanPath());
    JsonObject hookInstallationPlan =
        arguments.installHooks() ? read(arguments.hookInstallationPlanPath()) : null;
    List<String> failures = new ArrayList<>();

    verifySchema(runtimePlan, "runtime plan", 1, failures);
    verifySchema(boundaryPlan, "boundary plan", 1, failures);
    verifySchema(integrationPlan, "integration plan", 1, failures);
    verifySchema(executionPlan, "execution plan", 1, failures);
    verifyMilestone(executionPlan, "Milestone 8", "execution plan", failures);
    if (hookInstallationPlan != null) {
      verifySchema(hookInstallationPlan, "hook installation plan", 1, failures);
      verifyMilestone(hookInstallationPlan, "Target-4", "hook installation plan", failures);
    }

    MinecraftPlanFingerprint runtimeFingerprint =
        MinecraftPlanFingerprint.fromFile("runtime-plan", arguments.runtimePlanPath());
    MinecraftPlanFingerprint boundaryFingerprint =
        MinecraftPlanFingerprint.fromFile("boundary-plan", arguments.boundaryPath());
    MinecraftPlanFingerprint integrationFingerprint =
        MinecraftPlanFingerprint.fromFile("integration-plan", arguments.integrationPlanPath());
    MinecraftPlanFingerprint executionFingerprint =
        MinecraftPlanFingerprint.fromFile("execution-plan", arguments.executionPlanPath());
    MinecraftPlanFingerprint hookInstallationFingerprint =
        arguments.installHooks()
            ? MinecraftPlanFingerprint.fromFile(
                "hook-installation-plan", arguments.hookInstallationPlanPath())
            : null;

    if (arguments.verifyPlanFingerprints()) {
      verifyExpected(
          arguments.expectedRuntimeFingerprint(),
          runtimeFingerprint.sha256(),
          "runtime plan",
          failures);
      verifyExpected(
          arguments.expectedBoundaryFingerprint(),
          boundaryFingerprint.sha256(),
          "boundary plan",
          failures);
      verifyExpected(
          arguments.expectedIntegrationFingerprint(),
          integrationFingerprint.sha256(),
          "integration plan",
          failures);
      verifyExpected(
          arguments.expectedExecutionFingerprint(),
          executionFingerprint.sha256(),
          "execution plan",
          failures);
    }
    if (arguments.installHooks()) {
      verifyExpected(
          arguments.expectedHookInstallationPlanFingerprint(),
          hookInstallationFingerprint.sha256(),
          "hook installation plan",
          failures);
    }

    verifyEmbeddedFingerprint(
        executionPlan, "runtimePlanFingerprint", runtimeFingerprint.sha256(), failures);
    verifyEmbeddedFingerprint(
        executionPlan, "boundaryFingerprint", boundaryFingerprint.sha256(), failures);
    verifyEmbeddedFingerprint(
        executionPlan, "integrationPlanFingerprint", integrationFingerprint.sha256(), failures);

    failures.addAll(new MinecraftPlanDriftDetector().detect(integrationPlan, executionPlan));
    verifyRuntimeHashes(arguments.workingDirectory(), runtimePlan, failures);
    verifyModHashes(arguments.workingDirectory(), executionPlan, failures);
    if (hookInstallationPlan != null) {
      verifyHookInstallationPlan(hookInstallationPlan, executionPlan, failures);
    }

    if (!failures.isEmpty()) {
      throw new PlanDriftException(
          new MinecraftBootstrapFailure(
              "plan-drift",
              "Frozen bootstrap plans no longer match the current inputs.",
              failures));
    }

    return new VerifiedPlans(
        runtimePlan,
        boundaryPlan,
        integrationPlan,
        executionPlan,
        hookInstallationPlan,
        runtimeFingerprint,
        boundaryFingerprint,
        integrationFingerprint,
        executionFingerprint,
        hookInstallationFingerprint);
  }

  private JsonObject read(Path path) throws LoaderException {
    if (!Files.isRegularFile(path)) {
      throw new PlanDriftException(
          new MinecraftBootstrapFailure(
              "plan-missing",
              "Required frozen plan is missing: " + path.getFileName(),
              List.of(path.toString().replace('\\', '/'))));
    }
    try {
      return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException | RuntimeException exception) {
      throw new LoaderException(
          "Failed to read Minecraft bootstrap plan " + path.toString().replace('\\', '/'),
          exception);
    }
  }

  private void verifySchema(JsonObject object, String name, int expected, List<String> failures) {
    if (!object.has("schema") || object.get("schema").getAsInt() != expected) {
      failures.add(name + " schema mismatch");
    }
  }

  private void verifyMilestone(
      JsonObject object, String expected, String name, List<String> failures) {
    if (!object.has("milestoneName")
        || !expected.equals(object.get("milestoneName").getAsString())) {
      failures.add(name + " milestone mismatch");
    }
  }

  private void verifyExpected(String expected, String actual, String name, List<String> failures) {
    if (expected != null && !expected.isBlank() && !expected.equals(actual)) {
      failures.add(name + " fingerprint mismatch");
    }
  }

  private void verifyEmbeddedFingerprint(
      JsonObject executionPlan, String fieldName, String actual, List<String> failures) {
    if (!executionPlan.has(fieldName) || !executionPlan.getAsJsonObject(fieldName).has("sha256")) {
      failures.add("execution plan missing embedded " + fieldName);
      return;
    }
    if (!actual.equals(executionPlan.getAsJsonObject(fieldName).get("sha256").getAsString())) {
      failures.add("execution plan embedded " + fieldName + " does not match actual fingerprint");
    }
  }

  private void verifyRuntimeHashes(
      Path workingDirectory, JsonObject runtimePlan, List<String> failures) throws LoaderException {
    if (runtimePlan.has("serverJarPath") && runtimePlan.has("serverJarSha256")) {
      Path serverJar = resolve(workingDirectory, runtimePlan.get("serverJarPath").getAsString());
      if (Files.isRegularFile(serverJar)
          && !sha256(serverJar).equals(runtimePlan.get("serverJarSha256").getAsString())) {
        failures.add("runtime server jar hash drift detected");
      }
    }
    JsonArray classpathEntries = runtimePlan.getAsJsonArray("classpathEntries");
    if (classpathEntries != null) {
      classpathEntries.forEach(
          element -> {
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("sha256") || entry.get("sha256").isJsonNull()) {
              return;
            }
            Path runtimeJar = resolve(workingDirectory, entry.get("path").getAsString());
            try {
              if (Files.isRegularFile(runtimeJar)
                  && !sha256(runtimeJar).equals(entry.get("sha256").getAsString())) {
                failures.add(
                    "runtime classpath jar hash drift detected: "
                        + entry.get("path").getAsString());
              }
            } catch (LoaderException exception) {
              throw new RuntimeException(exception);
            }
          });
    }
  }

  private void verifyModHashes(
      Path workingDirectory, JsonObject executionPlan, List<String> failures)
      throws LoaderException {
    JsonArray mods = executionPlan.getAsJsonArray("acceptedExecutableMods");
    if (mods == null) {
      return;
    }
    for (int index = 0; index < mods.size(); index++) {
      JsonObject mod = mods.get(index).getAsJsonObject();
      Path modJar = resolve(workingDirectory, mod.get("modJarPath").getAsString());
      if (!Files.isRegularFile(modJar)) {
        failures.add("approved mod jar missing: " + mod.get("modId").getAsString());
        continue;
      }
      if (!sha256(modJar).equals(mod.get("modJarSha256").getAsString())) {
        failures.add("approved mod jar hash drift detected: " + mod.get("modId").getAsString());
      }
    }
  }

  private void verifyHookInstallationPlan(
      JsonObject hookInstallationPlan, JsonObject executionPlan, List<String> failures) {
    if (!hookInstallationPlan.has("gatePassed")
        || !hookInstallationPlan.get("gatePassed").getAsBoolean()) {
      failures.add("hook installation plan gate failed");
    }
    if (!hookInstallationPlan.has("installationPlanned")
        || !hookInstallationPlan.get("installationPlanned").getAsBoolean()) {
      failures.add("hook installation plan is not marked as planned");
    }
    if (!hookInstallationPlan.has("installationMode")
        || !"launch-boundary-main-wrapper"
            .equals(hookInstallationPlan.get("installationMode").getAsString())) {
      failures.add("hook installation plan mode mismatch");
    }
    JsonArray plannedHooks = hookInstallationPlan.getAsJsonArray("plannedHooks");
    if (plannedHooks == null || plannedHooks.size() != 1) {
      failures.add("hook installation plan must contain exactly one hook");
    }
    if (!hookInstallationPlan.has("minecraftMainClass")
        || !executionPlan.has("minecraftMainClass")
        || !hookInstallationPlan
            .get("minecraftMainClass")
            .getAsString()
            .equals(executionPlan.get("minecraftMainClass").getAsString())) {
      failures.add("hook installation plan minecraft main class mismatch");
    }
    if (!hookInstallationPlan.has("minecraftVersion")
        || !executionPlan.has("resolvedMinecraftVersion")
        || !hookInstallationPlan
            .get("minecraftVersion")
            .getAsString()
            .equals(executionPlan.get("resolvedMinecraftVersion").getAsString())) {
      failures.add("hook installation plan minecraft version mismatch");
    }
    if (!hookInstallationPlan.has("side")
        || !executionPlan.has("side")
        || !hookInstallationPlan
            .get("side")
            .getAsString()
            .equals(executionPlan.get("side").getAsString())) {
      failures.add("hook installation plan side mismatch");
    }
  }

  private Path resolve(Path workingDirectory, String serializedPath) {
    Path path = Path.of(serializedPath);
    return path.isAbsolute()
        ? path.normalize()
        : workingDirectory.resolve(path).toAbsolutePath().normalize();
  }

  private String sha256(Path path) throws LoaderException {
    try {
      return HexFormat.of()
          .formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    } catch (Exception exception) {
      throw new LoaderException("Failed to hash " + path.toString().replace('\\', '/'), exception);
    }
  }

  public record VerifiedPlans(
      JsonObject runtimePlan,
      JsonObject boundaryPlan,
      JsonObject integrationPlan,
      JsonObject executionPlan,
      JsonObject hookInstallationPlan,
      MinecraftPlanFingerprint runtimeFingerprint,
      MinecraftPlanFingerprint boundaryFingerprint,
      MinecraftPlanFingerprint integrationFingerprint,
      MinecraftPlanFingerprint executionFingerprint,
      MinecraftPlanFingerprint hookInstallationPlanFingerprint) {}

  public static final class PlanDriftException extends RuntimeException {
    private final MinecraftBootstrapFailure failure;

    public PlanDriftException(MinecraftBootstrapFailure failure) {
      super(failure.message());
      this.failure = failure;
    }

    public MinecraftBootstrapFailure failure() {
      return failure;
    }
  }
}
