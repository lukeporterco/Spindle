package com.spindle.core.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.api.LoaderApi;
import com.spindle.api.ModContext;
import com.spindle.api.exception.CapabilityDeniedException;
import com.spindle.api.exception.ConfigAccessException;
import com.spindle.api.exception.ServiceAccessException;
import com.spindle.api.exception.SpindleApiException;
import com.spindle.api.service.ServiceRegistry;
import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import com.spindle.core.runtime.closure.RuntimeClosureContract;
import com.spindle.core.runtime.closure.RuntimeClosurePlanner;
import com.spindle.core.security.SecurityFinding;
import com.spindle.core.security.SecurityGate;
import com.spindle.core.security.SecurityLocation;
import com.spindle.core.security.SecurityPolicy;
import com.spindle.core.security.SecurityPolicyFingerprint;
import com.spindle.core.security.SecuritySeverity;
import com.spindle.core.security.SecurityValidationResult;
import com.spindle.core.security.tool.RestrictedToolExecutionMode;
import com.spindle.core.security.tool.RestrictedToolResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoaderApiHardeningContractTest {
  @TempDir Path tempDirectory;

  @Test
  void loaderApiConstantsRemainStable() {
    assertEquals(1, LoaderApi.RUNTIME_API_VERSION);
    assertEquals(6, CompiledModpackProfile.SCHEMA_VERSION);
    assertEquals(2, RuntimeClosureContract.CONTRACT_VERSION);
    assertEquals(2, RuntimeCapabilityCatalog.CATALOG_VERSION);
    assertEquals(8, SecurityPolicy.standard().securityPolicyVersion());
    assertEquals(5, SecurityPolicy.standard().permissionPolicyVersion());
    assertEquals("spindle-loader-runtime-api-stabilized", LoaderApi.API_STATUS);
    assertEquals("runtime-facing-spindle-loader-api", LoaderApi.API_SCOPE);
    assertEquals("minecraft-as-target-not-foundation", LoaderApi.TARGET_MODEL);
    assertFalse(LoaderApi.SANDBOXED);
    assertEquals("not-sandboxed", LoaderApi.SANDBOX_CLAIM);
  }

  @Test
  void publicExceptionsExposeStableFieldsAndCauses() {
    assertTrue(SpindleApiException.class.isAssignableFrom(CapabilityDeniedException.class));
    assertTrue(SpindleApiException.class.isAssignableFrom(ConfigAccessException.class));
    assertTrue(SpindleApiException.class.isAssignableFrom(ServiceAccessException.class));
    assertTrue(RuntimeException.class.isAssignableFrom(SpindleApiException.class));

    CapabilityDeniedException capabilityException =
        new CapabilityDeniedException("examplemod", "storage.data", "dataDirectory()", "denied");
    assertEquals("examplemod", capabilityException.modId());
    assertEquals("storage.data", capabilityException.capability());
    assertEquals("dataDirectory()", capabilityException.methodName());

    ConfigAccessException configException =
        new ConfigAccessException("examplemod", "mode", "bad config");
    assertEquals("examplemod", configException.modId());
    assertEquals("mode", configException.key());

    ServiceAccessException serviceException =
        new ServiceAccessException("examplemod", "sample:greeting", "bad service");
    assertEquals("examplemod", serviceException.modId());
    assertEquals("sample:greeting", serviceException.serviceId());

    IllegalStateException cause = new IllegalStateException("cause");
    assertEquals(cause, new SpindleApiException("api", cause).getCause());
    assertEquals(
        cause, new ConfigAccessException("examplemod", "mode", "config", cause).getCause());
    assertEquals(
        cause,
        new ServiceAccessException("examplemod", "sample:greeting", "service", cause).getCause());
  }

  @Test
  void modContextDefaultHelpersArePredictable() {
    ModContext context =
        new MinimalModContext(tempDirectory) {
          @Override
          public Set<String> grantedCapabilities() {
            return Set.of("storage.data");
          }
        };

    assertTrue(context.hasCapability("storage.data"));
    assertFalse(context.hasCapability("storage.cache"));
    assertDoesNotThrow(() -> context.requireCapability("storage.data"));

    CapabilityDeniedException exception =
        assertThrows(
            CapabilityDeniedException.class, () -> context.requireCapability("storage.cache"));
    assertEquals("requireCapability", exception.methodName());
  }

  @Test
  void defaultConfigAndServiceFallbacksAreSafe() {
    ModContext context = new MinimalModContext(tempDirectory);

    assertTrue(context.config().keys().isEmpty());
    assertThrows(UnsupportedOperationException.class, () -> context.config().keys().add("example"));
    assertFalse(context.config().writable());

    ConfigAccessException configException =
        assertThrows(ConfigAccessException.class, () -> context.config().getString("mode"));
    assertFalse(configException.getMessage().contains("Runtime-4"));

    assertTrue(context.services().availableServiceIds().isEmpty());
    assertThrows(
        UnsupportedOperationException.class,
        () -> context.services().availableServiceIds().add("example:missing"));
    assertTrue(context.services().find("example:missing", Object.class).isEmpty());
    assertThrows(
        ServiceAccessException.class,
        () -> context.services().require("example:missing", Object.class));
  }

  @Test
  void emptyServiceRegistryFallbacksAreDeterministic() {
    ServiceRegistry registry = ServiceRegistry.empty();

    assertTrue(registry.availableServiceIds().isEmpty());
    assertThrows(
        UnsupportedOperationException.class,
        () -> registry.availableServiceIds().add("example:missing"));
    assertFalse(registry.hasService("example:missing"));
    assertTrue(registry.find("example:missing", Object.class).isEmpty());

    ServiceAccessException exception =
        assertThrows(
            ServiceAccessException.class, () -> registry.require("example:missing", Object.class));
    assertEquals("example:missing", exception.serviceId());
    assertEquals("unavailable", exception.modId());
  }

  @Test
  void runtimeClosureBoundaryStillMatchesSecurityPolicyShadowedClasses() {
    RuntimeClosureContract contract = new RuntimeClosurePlanner().plan();
    List<String> stableCandidates = contract.loaderApiBoundary().stableCandidates();
    List<String> shadowedClasses = SecurityPolicy.standard().knownShadowedClasses();

    assertTrue(shadowedClasses.containsAll(stableCandidates));
    assertFalse(stableCandidates.contains("com.spindle.api.minecraft.MinecraftServerModContext"));
    assertFalse(
        stableCandidates.contains("com.spindle.api.minecraft.MinecraftServerModInitializer"));
    assertEquals(
        List.of(
            "com.spindle.api.minecraft.MinecraftServerModContext",
            "com.spindle.api.minecraft.MinecraftServerModInitializer"),
        contract.loaderApiBoundary().deferredReview());
  }

  @Test
  void securityGateMessageUsesStandardRuntimeLifecycleWording() {
    SecurityValidationResult validationResult =
        new SecurityValidationResult(
            new SecurityPolicyFingerprint("fingerprint"),
            new RestrictedToolResult(
                RestrictedToolExecutionMode.RESTRICTED_CHILD_JVM,
                "static-risk-scan",
                RestrictedToolResult.STATUS_PASSED,
                ".spindle/security-tools/static-risk-scan/output.json",
                null,
                List.of()),
            List.of(),
            List.of(),
            new com.spindle.core.security.trust.ArtifactTrustSummary(0, 0, 0, 0),
            com.spindle.core.security.risk.StaticRiskSummary.EMPTY,
            List.of(),
            List.of(
                new SecurityFinding(
                    com.spindle.core.security.SecurityRuleId.SEC_TOOL_001,
                    SecuritySeverity.FATAL,
                    "examplemod",
                    SecurityLocation.of("tool-output", "output.json"),
                    "fatal tool failure",
                    "fix it")));

    Exception exception =
        assertThrows(
            Exception.class,
            () -> new SecurityGate().ensureLifecycleExecutionAllowed(validationResult));
    assertTrue(exception.getMessage().contains("standard runtime lifecycle execution"));
    assertFalse(exception.getMessage().contains("Runtime-1 lifecycle execution"));
  }

  @Test
  void loaderApiStableSourceFilesAreExplicit() throws IOException {
    Path sourceRoot = loaderApiSourceRoot();
    TreeSet<String> classNames = new TreeSet<>();
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      paths
          .filter(path -> path.toString().endsWith(".java"))
          .filter(path -> includeStableSource(sourceRoot, path))
          .map(path -> toClassName(sourceRoot, path))
          .forEach(classNames::add);
    }

    assertEquals(
        List.of(
            "com.spindle.api.LoaderApi",
            "com.spindle.api.ModContext",
            "com.spindle.api.ModInitializer",
            "com.spindle.api.config.ModConfig",
            "com.spindle.api.exception.CapabilityDeniedException",
            "com.spindle.api.exception.ConfigAccessException",
            "com.spindle.api.exception.ServiceAccessException",
            "com.spindle.api.exception.SpindleApiException",
            "com.spindle.api.lifecycle.LifecyclePhase",
            "com.spindle.api.service.ServiceRegistry"),
        classNames.stream().toList());
  }

  private boolean includeStableSource(Path sourceRoot, Path path) {
    String relative = sourceRoot.relativize(path).toString().replace('\\', '/');
    return !relative.equals("package-info.java")
        && !relative.endsWith("/package-info.java")
        && !relative.startsWith("minecraft/")
        && !relative.equals("config/EmptyModConfig.java");
  }

  private String toClassName(Path sourceRoot, Path path) {
    String relative = sourceRoot.relativize(path).toString().replace('\\', '.').replace('/', '.');
    return "com.spindle.api." + relative.substring(0, relative.length() - ".java".length());
  }

  private Path loaderApiSourceRoot() {
    Path sourceRoot = Paths.get("spindle-loader-api/src/main/java/com/spindle/api");
    if (Files.exists(sourceRoot)) {
      return sourceRoot;
    }
    return Paths.get("../spindle-loader-api/src/main/java/com/spindle/api");
  }

  private static class MinimalModContext implements ModContext {
    private final Path root;

    MinimalModContext(Path root) {
      this.root = root;
    }

    @Override
    public String modId() {
      return "examplemod";
    }

    @Override
    public String modVersion() {
      return "1.0.0";
    }

    @Override
    public String loaderVersion() {
      return "0.1.0";
    }

    @Override
    public String gameId() {
      return "sample";
    }

    @Override
    public String gameVersion() {
      return "1.0.0";
    }

    @Override
    public String side() {
      return "universal";
    }

    @Override
    public Path workingDirectory() {
      return root;
    }

    @Override
    public Path configDirectory() {
      return root.resolve("config/examplemod");
    }

    @Override
    public Path dataDirectory() {
      return root.resolve("data/examplemod");
    }

    @Override
    public Path cacheDirectory() {
      return root.resolve("cache/examplemod");
    }

    @Override
    public Path generatedDirectory() {
      return root.resolve("generated/examplemod");
    }
  }
}
