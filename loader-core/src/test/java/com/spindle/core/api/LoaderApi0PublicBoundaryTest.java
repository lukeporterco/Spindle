package com.spindle.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.spindle.api.LoaderApi;
import com.spindle.api.ModContext;
import com.spindle.api.config.ModConfig;
import com.spindle.api.exception.CapabilityDeniedException;
import com.spindle.api.exception.ConfigAccessException;
import com.spindle.api.exception.ServiceAccessException;
import com.spindle.api.service.ServiceRegistry;
import com.spindle.core.runtime.DefaultModContext;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoaderApi0PublicBoundaryTest {
  @TempDir Path tempDirectory;

  @Test
  void loaderApiMetadataDescribesRuntimeFacingBoundary() {
    assertEquals(1, LoaderApi.RUNTIME_API_VERSION);
    assertEquals("runtime-api-stabilized", LoaderApi.API_STATUS);
    assertEquals("runtime-facing-loader-api", LoaderApi.API_SCOPE);
    assertEquals("minecraft-as-target-not-foundation", LoaderApi.TARGET_MODEL);
    assertFalse(LoaderApi.SANDBOXED);
    assertEquals("not-sandboxed", LoaderApi.SANDBOX_CLAIM);
  }

  @Test
  void modContextRequireCapabilityThrowsCapabilityDeniedException() {
    ModContext context = context(Set.of());

    CapabilityDeniedException exception =
        assertThrows(
            CapabilityDeniedException.class, () -> context.requireCapability("storage.data"));

    assertEquals("examplemod", exception.modId());
    assertEquals("storage.data", exception.capability());
    assertEquals("requireCapability", exception.methodName());
  }

  @Test
  void modContextGrantedCapabilitiesAreDeterministic() {
    ModContext context = context(Set.of("storage.generated", "config.read", "storage.data"));

    List<String> granted = new ArrayList<>(context.grantedCapabilities());
    assertEquals(List.of("config.read", "storage.data", "storage.generated"), granted);
    assertThrows(
        UnsupportedOperationException.class,
        () -> context.grantedCapabilities().add("storage.cache"));
  }

  @Test
  void storageAccessDeniedThrowsCapabilityDeniedException() {
    ModContext context = context(Set.of());

    CapabilityDeniedException exception =
        assertThrows(CapabilityDeniedException.class, context::configDirectory);

    assertEquals("examplemod", exception.modId());
    assertEquals("storage.config", exception.capability());
    assertEquals("configDirectory()", exception.methodName());
  }

  @Test
  void emptyConfigThrowsConfigAccessException() {
    ModConfig config = ModConfig.empty();

    ConfigAccessException readException =
        assertThrows(ConfigAccessException.class, () -> config.getString("mode"));
    assertEquals("mode", readException.key());
    assertFalse(readException.getMessage().contains("Runtime-4"));

    ConfigAccessException writeException =
        assertThrows(ConfigAccessException.class, () -> config.setString("mode", "x"));
    assertEquals("mode", writeException.key());
    assertFalse(writeException.getMessage().contains("Runtime-4"));
  }

  @Test
  void emptyServiceRegistryThrowsServiceAccessException() {
    ServiceRegistry registry = ServiceRegistry.empty();

    ServiceAccessException exception =
        assertThrows(
            ServiceAccessException.class, () -> registry.require("example:missing", Object.class));

    assertEquals("example:missing", exception.serviceId());
  }

  private DefaultModContext context(Set<String> grantedCapabilities) {
    return new DefaultModContext(
        "examplemod",
        "1.0.0",
        "0.1.0",
        "sample",
        "1.0.0",
        "universal",
        tempDirectory,
        grantedCapabilities,
        ModConfig.empty(),
        ServiceRegistry.empty(),
        tempDirectory.resolve("config/examplemod"),
        tempDirectory.resolve("data/examplemod"),
        tempDirectory.resolve("cache/examplemod"),
        tempDirectory.resolve("generated/examplemod"));
  }
}
