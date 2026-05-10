package com.spindle.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.api.exception.ServiceAccessException;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlternateGreetingService;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.GreetingConsumerLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.GreetingService;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.GreetingServiceImpl;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.GreetingServiceImplTwo;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.OptionalGreetingConsumerLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.SecurityReportAwareLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.UndeclaredServiceConsumerLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime3ServiceRegistryContractTest {
  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIncludesRuntimeThreeServiceContract() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of("service.provide"),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(),
        List.of("service.consume"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), true)),
        false);

    execute(true);

    JsonObject profile = readCompiledProfile();
    assertEquals(6, profile.get("schemaVersion").getAsInt());

    JsonObject services = profile.getAsJsonObject("services");
    assertEquals(1, services.get("contractVersion").getAsInt());
    assertEquals(
        "available",
        services.getAsJsonArray("mods").get(1).getAsJsonObject()
            .getAsJsonArray("provides")
            .get(0)
            .getAsJsonObject()
            .get("state")
            .getAsString());
    assertEquals(
        "bound",
        services.getAsJsonArray("mods").get(0).getAsJsonObject()
            .getAsJsonArray("consumes")
            .get(0)
            .getAsJsonObject()
            .get("state")
            .getAsString());
    assertEquals(
        "bound",
        services.getAsJsonArray("bindings").get(0).getAsJsonObject().get("state").getAsString());
    assertEquals(0, services.getAsJsonObject("summary").get("fatalCount").getAsInt());

    Map<String, JsonObject> permissionMods =
        permissionModsById(profile.getAsJsonObject("permissions").getAsJsonArray("mods"));
    assertEquals(
        "granted",
        grantsByCapability(permissionMods.get("provider").getAsJsonArray("grants"))
            .get("service.provide")
            .get("state")
            .getAsString());
    assertEquals(
        "granted",
        grantsByCapability(permissionMods.get("consumer").getAsJsonArray("grants"))
            .get("service.consume")
            .get("state")
            .getAsString());

    JsonObject securityReport = readSecurityReport();
    assertFalse(ruleIds(securityReport).contains(SecurityRuleId.SEC_PERM_001.id()));
  }

  @Test
  void serviceRegistryReturnsDeclaredBoundServiceDuringLifecycle() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of("BOOTSTRAP", List.of(GreetingConsumerLifecycle.class.getName() + "::bootstrap")),
        List.of("service.consume", "storage.generated"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), true)),
        true);

    execute(false);

    assertEquals(
        "hello-from-provider",
        Files.readString(
                tempDirectory.resolve("generated/consumer/greeting.marker"), StandardCharsets.UTF_8)
            .trim());
  }

  @Test
  void optionalUnboundServiceDoesNotBlockAndFindReturnsEmpty() throws Exception {
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(
            "BOOTSTRAP", List.of(OptionalGreetingConsumerLifecycle.class.getName() + "::bootstrap")),
        List.of("service.consume", "storage.generated"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), false)),
        true);

    execute(false);

    assertEquals(
        "missing",
        Files.readString(
                tempDirectory.resolve("generated/consumer/optional-greeting.marker"),
                StandardCharsets.UTF_8)
            .trim());
    assertEquals(
        "optional-unbound",
        readCompiledProfile()
            .getAsJsonObject("services")
            .getAsJsonArray("bindings")
            .get(0)
            .getAsJsonObject()
            .get("state")
            .getAsString());

    JsonObject qualityReport = readQualityReport();
    assertTrue(findingCodes(qualityReport, "warningFindings").contains("service.optional_unbound"));
    assertFalse(findingCodes(qualityReport, "fatalFindings").contains("service.optional_unbound"));
  }

  @Test
  void requiredUnboundServiceBlocksExecutionAfterValidation() throws Exception {
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of("BOOTSTRAP", List.of(GreetingConsumerLifecycle.class.getName() + "::bootstrap")),
        List.of("service.consume", "storage.generated"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), true)),
        true);

    execute(true);

    assertTrue(Files.exists(tempDirectory.resolve("spindle.profile.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.quality-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.lifecycle-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.security-report.json")));
    assertEquals(
        "required-unbound",
        readCompiledProfile()
            .getAsJsonObject("services")
            .getAsJsonArray("bindings")
            .get(0)
            .getAsJsonObject()
            .get("state")
            .getAsString());
    assertTrue(findingCodes(readQualityReport(), "fatalFindings").contains("service.required_unbound"));

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("Runtime service contract has fatal findings"));
    assertFalse(Files.exists(tempDirectory.resolve("generated/consumer/greeting.marker")));
  }

  @Test
  void duplicateProvidersCreateProviderConflict() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider-a.jar"),
        "providera",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createProviderModJar(
        tempDirectory.resolve("mods/provider-b.jar"),
        "providerb",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImplTwo.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(),
        List.of("service.consume"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), true)),
        false);

    execute(true);

    JsonObject services = readCompiledProfile().getAsJsonObject("services");
    assertEquals(
        List.of("conflict", "conflict"),
        providerStates(services, "providera", "providerb"));
    assertEquals(
        "provider-conflict",
        services.getAsJsonArray("bindings").get(0).getAsJsonObject().get("state").getAsString());

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("Runtime service contract has fatal findings"));
  }

  @Test
  void typeMismatchCreatesFatalBinding() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(),
        List.of("service.consume"),
        true,
        List.of(serviceConsumer(AlternateGreetingService.class.getName(), true)),
        false);

    execute(true);

    assertEquals(
        "type-mismatch",
        readCompiledProfile()
            .getAsJsonObject("services")
            .getAsJsonArray("bindings")
            .get(0)
            .getAsJsonObject()
            .get("state")
            .getAsString());

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("typeMismatches=1"));
  }

  @Test
  void providerImplementationMustBeOwnedByProviderMod() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())),
        Map.of());
    createForeignOwnerModJar(
        tempDirectory.resolve("mods/foreign-owner.jar"),
        "foreignowner",
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class),
            resourceName(GreetingServiceImpl.class),
            readClassBytes(GreetingServiceImpl.class)));

    execute(true);

    JsonObject providerPlan =
        serviceModPlan(readCompiledProfile().getAsJsonObject("services"), "provider")
            .getAsJsonArray("provides")
            .get(0)
            .getAsJsonObject();
    assertEquals("implementation-not-owned", providerPlan.get("state").getAsString());

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("implementationOwnershipViolations=1"));
  }

  @Test
  void undeclaredServiceLookupFailsClearly() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(
            "BOOTSTRAP",
            List.of(UndeclaredServiceConsumerLifecycle.class.getName() + "::bootstrap")),
        List.of(),
        false,
        List.of(),
        false);

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("Lifecycle handler failed for mod `consumer`"));
    assertTrue(exception.getCause() instanceof LoaderException);
    assertInstanceOf(ServiceAccessException.class, exception.getCause().getCause());
    assertTrue(
        exception
            .getCause()
            .getCause()
            .getMessage()
            .contains("it was not declared in services.consumes"));
  }

  @Test
  void duplicateConsumedServiceIdsWithinOneModAreRejected() throws Exception {
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(),
        List.of("service.consume"),
        false,
        List.of(
            serviceConsumer(GreetingService.class.getName(), true),
            serviceConsumer(GreetingService.class.getName(), false)),
        false);

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(true));
    assertTrue(exception.getMessage().contains(SecurityRuleId.SEC_METADATA_001.id()));
    assertTrue(exception.getMessage().contains("duplicate consumed service id `sample:greeting`"));
  }

  @Test
  void schemaFourCacheInvalidatesCleanlyAgainstSchemaFiveReader() throws Exception {
    createProviderModJar(
        tempDirectory.resolve("mods/provider.jar"),
        "provider",
        List.of(),
        List.of(serviceProvider(GreetingService.class.getName(), GreetingServiceImpl.class.getName())));
    createConsumerModJar(
        tempDirectory.resolve("mods/consumer.jar"),
        "consumer",
        Map.of(),
        List.of("service.consume"),
        true,
        List.of(serviceConsumer(GreetingService.class.getName(), true)),
        false);

    execute(true);

    JsonObject cachedProfile =
        JsonParser.parseString(Files.readString(cachedProfilePath(), StandardCharsets.UTF_8))
            .getAsJsonObject();
    cachedProfile.addProperty("schemaVersion", 4);
    cachedProfile.remove("services");
    Files.writeString(cachedProfilePath(), cachedProfile.toString(), StandardCharsets.UTF_8);

    execute(true);

    assertEquals(
        "schema mismatch",
        readCompiledProfile().getAsJsonObject("cache").get("reason").getAsString());
  }

  private void createProviderModJar(
      Path jarPath,
      String modId,
      List<String> permissions,
      List<ServiceProviderDeclaration> providers)
      throws IOException {
    createProviderModJar(jarPath, modId, permissions, providers, providerEntries(providers));
  }

  private void createProviderModJar(
      Path jarPath,
      String modId,
      List<String> permissions,
      List<ServiceProviderDeclaration> providers,
      Map<String, byte[]> entries)
      throws IOException {
    createModJar(
        jarPath,
        schemaTwoMetadata(
            modId,
            Map.of(),
            permissions,
            false,
            false,
            false,
            false,
            providers,
            List.of()),
        entries);
  }

  private void createConsumerModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      boolean generated,
      List<ServiceConsumerDeclaration> consumers,
      boolean includeGreetingFixtures)
      throws IOException {
    Map<String, byte[]> entries = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : lifecycle.entrySet()) {
      for (String declaration : entry.getValue()) {
        String className = declaration.substring(0, declaration.indexOf("::"));
        try {
          Class<?> type = Class.forName(className);
          entries.put(resourceName(type), readClassBytes(type));
        } catch (ClassNotFoundException exception) {
          throw new IOException("Missing lifecycle fixture class " + className, exception);
        }
      }
    }
    createModJar(
        jarPath,
        schemaTwoMetadata(
            modId,
            lifecycle,
            permissions,
            false,
            false,
            false,
            generated,
            List.of(),
            consumers),
        entries);
  }

  private void createForeignOwnerModJar(Path jarPath, String modId, Map<String, byte[]> entries)
      throws IOException {
    createModJar(
        jarPath,
        schemaTwoMetadata(
            modId,
            Map.of(
                "BOOTSTRAP",
                List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
            List.of(),
            false,
            false,
            false,
            false,
            List.of(),
            List.of()),
        entries);
  }

  private Map<String, byte[]> providerEntries(List<ServiceProviderDeclaration> providers)
      throws IOException {
    Map<String, byte[]> entries = new LinkedHashMap<>();
    for (ServiceProviderDeclaration provider : providers) {
      try {
        Class<?> implementationClass = Class.forName(provider.implementation());
        entries.put(resourceName(implementationClass), readClassBytes(implementationClass));
      } catch (ClassNotFoundException exception) {
        throw new IOException("Missing provider implementation class " + provider.implementation(), exception);
      }
    }
    return entries;
  }

  private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries)
      throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      jarOutputStream.putNextEntry(new JarEntry("loader.mod.json"));
      jarOutputStream.write(metadataJson.getBytes(StandardCharsets.UTF_8));
      jarOutputStream.closeEntry();
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    }
  }

  private String schemaTwoMetadata(
      String modId,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      boolean config,
      boolean data,
      boolean cache,
      boolean generated,
      List<ServiceProviderDeclaration> providers,
      List<ServiceConsumerDeclaration> consumers) {
    return """
        {
          "schema": 2,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          },
          "breaks": {},
          "lifecycle": %s,
          "permissions": %s,
          "storage": {
            "config": %s,
            "data": %s,
            "cache": %s,
            "generated": %s
          },
          "services": %s
        }
        """
        .formatted(
            modId,
            lifecycleJson(lifecycle),
            toJsonArray(permissions),
            Boolean.toString(config),
            Boolean.toString(data),
            Boolean.toString(cache),
            Boolean.toString(generated),
            servicesJson(providers, consumers));
  }

  private String lifecycleJson(Map<String, List<String>> lifecycle) {
    StringBuilder builder = new StringBuilder("{");
    boolean firstPhase = true;
    for (Map.Entry<String, List<String>> entry :
        lifecycle.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!firstPhase) {
        builder.append(", ");
      }
      builder.append("\"").append(entry.getKey()).append("\": ").append(toJsonArray(entry.getValue()));
      firstPhase = false;
    }
    builder.append("}");
    return builder.toString();
  }

  private String servicesJson(
      List<ServiceProviderDeclaration> providers, List<ServiceConsumerDeclaration> consumers) {
    return """
        {
          "provides": %s,
          "consumes": %s
        }
        """
        .formatted(providerArrayJson(providers), consumerArrayJson(consumers));
  }

  private String providerArrayJson(List<ServiceProviderDeclaration> providers) {
    return providers.stream()
        .map(
            provider ->
                """
                {
                  "id": "%s",
                  "type": "%s",
                  "implementation": "%s"
                }
                """
                    .formatted(provider.id(), provider.type(), provider.implementation())
                    .replace(System.lineSeparator(), "\n")
                    .trim())
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private String consumerArrayJson(List<ServiceConsumerDeclaration> consumers) {
    return consumers.stream()
        .map(
            consumer ->
                """
                {
                  "id": "%s",
                  "type": "%s",
                  "required": %s
                }
                """
                    .formatted(consumer.id(), consumer.type(), Boolean.toString(consumer.required()))
                    .replace(System.lineSeparator(), "\n")
                    .trim())
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private ServiceProviderDeclaration serviceProvider(String type, String implementation) {
    return new ServiceProviderDeclaration("sample:greeting", type, implementation);
  }

  private ServiceConsumerDeclaration serviceConsumer(String type, boolean required) {
    return new ServiceConsumerDeclaration("sample:greeting", type, required);
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readQualityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.quality-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private Path cachedProfilePath() throws IOException {
    JsonObject profile = readCompiledProfile();
    return tempDirectory
        .resolve(".spindle")
        .resolve("profile-cache")
        .resolve(profile.get("inputFingerprint").getAsString())
        .resolve("spindle.profile.json");
  }

  private Map<String, JsonObject> permissionModsById(JsonArray mods) {
    Map<String, JsonObject> values = new LinkedHashMap<>();
    for (var element : mods) {
      JsonObject mod = element.getAsJsonObject();
      values.put(mod.get("modId").getAsString(), mod);
    }
    return Map.copyOf(values);
  }

  private Map<String, JsonObject> grantsByCapability(JsonArray grants) {
    Map<String, JsonObject> values = new LinkedHashMap<>();
    for (var element : grants) {
      JsonObject grant = element.getAsJsonObject();
      values.put(grant.get("capability").getAsString(), grant);
    }
    return Map.copyOf(values);
  }

  private List<String> providerStates(JsonObject services, String... modIds) {
    List<String> states = new ArrayList<>();
    for (String modId : modIds) {
      states.add(
          serviceModPlan(services, modId)
              .getAsJsonArray("provides")
              .get(0)
              .getAsJsonObject()
              .get("state")
              .getAsString());
    }
    return states;
  }

  private JsonObject serviceModPlan(JsonObject services, String modId) {
    for (var element : services.getAsJsonArray("mods")) {
      JsonObject modPlan = element.getAsJsonObject();
      if (modId.equals(modPlan.get("modId").getAsString())) {
        return modPlan;
      }
    }
    throw new IllegalArgumentException("Missing service mod plan for " + modId);
  }

  private List<String> ruleIds(JsonObject report) {
    List<String> values = new ArrayList<>();
    for (var element : report.getAsJsonArray("findings")) {
      values.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return values;
  }

  private List<String> findingCodes(JsonObject report, String fieldName) {
    List<String> values = new ArrayList<>();
    for (var element : report.getAsJsonArray(fieldName)) {
      values.add(element.getAsJsonObject().get("code").getAsString());
    }
    return values;
  }

  private String toJsonArray(List<String> values) {
    return values.stream()
        .map(value -> "\"" + value + "\"")
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private String execute(boolean validateOnly) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      return captureStdout(
          () ->
              new LoaderApplication()
                  .run(
                      tempDirectory,
                      new LaunchArguments(
                          ValidationGameMain.class.getName(),
                          "sample",
                          List.of(),
                          validateOnly,
                          false,
                          false,
                          false,
                          null,
                          null,
                          null,
                          false),
                      sink));
    } finally {
      sink.write();
    }
  }

  private String captureStdout(ThrowingRunnable runnable) throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (PrintStream replacement = new PrintStream(outputStream, true, StandardCharsets.UTF_8)) {
      System.setOut(replacement);
      runnable.run();
    } finally {
      System.setOut(originalOut);
    }
    return outputStream.toString(StandardCharsets.UTF_8);
  }

  private byte[] readClassBytes(Class<?> type) throws IOException {
    try (var inputStream = type.getClassLoader().getResourceAsStream(resourceName(type))) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName(type));
      }
      return inputStream.readAllBytes();
    }
  }

  private String resourceName(Class<?> type) {
    return type.getName().replace('.', '/') + ".class";
  }

  private record ServiceProviderDeclaration(String id, String type, String implementation) {}

  private record ServiceConsumerDeclaration(String id, String type, boolean required) {}

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static final class ValidationGameMain {
    public static void main(String[] args) {}
  }
}
