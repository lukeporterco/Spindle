package com.spindle.core.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.capability.RuntimeCapabilityGrant;
import com.spindle.core.runtime.capability.RuntimeCapabilityModPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilityPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilitySummary;
import com.spindle.core.runtime.closure.RuntimeClosureContract;
import com.spindle.core.runtime.closure.RuntimeClosureGate;
import com.spindle.core.runtime.closure.RuntimeClosureLoaderApiBoundary;
import com.spindle.core.runtime.closure.RuntimeClosureSummary;
import com.spindle.core.runtime.closure.RuntimeClosureSurface;
import com.spindle.core.runtime.config.RuntimeConfigContract;
import com.spindle.core.runtime.config.RuntimeConfigEntryPlan;
import com.spindle.core.runtime.config.RuntimeConfigModPlan;
import com.spindle.core.runtime.config.RuntimeConfigSummary;
import com.spindle.core.runtime.service.RuntimeServiceBinding;
import com.spindle.core.runtime.service.RuntimeServiceConsumerPlan;
import com.spindle.core.runtime.service.RuntimeServiceContract;
import com.spindle.core.runtime.service.RuntimeServiceModPlan;
import com.spindle.core.runtime.service.RuntimeServiceProviderPlan;
import com.spindle.core.runtime.service.RuntimeServiceSummary;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CompiledModpackProfileReader {
  public CompiledModpackProfile read(Path path) throws LoaderException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      return new CompiledModpackProfile(
          requiredInt(root, "schemaVersion"),
          requiredString(root, "profileKind"),
          requiredString(root, "fingerprint"),
          requiredString(root, "inputFingerprint"),
          requiredString(root, "runtimePolicyFingerprint"),
          readCache(requiredObject(root, "cache")),
          readLoader(requiredObject(root, "loader")),
          readGame(requiredObject(root, "game")),
          readMetadata(requiredObject(root, "metadata")),
          readMods(requiredArray(root, "mods")),
          readStringArray(requiredArray(root, "resolvedOrder")),
          readClasspath(requiredArray(root, "classpath")),
          readOwnership(requiredObject(root, "ownership")),
          readLockfile(requiredObject(root, "lockfile")),
          readPermissions(requiredObject(root, "permissions")),
          readConfig(root.has("config") ? root.getAsJsonObject("config") : null),
          readServices(root.has("services") ? root.getAsJsonObject("services") : null),
          readRuntimeClosure(root.has("runtimeClosure") ? root.getAsJsonObject("runtimeClosure") : null),
          readLifecycle(requiredObject(root, "lifecycle")),
          readContexts(requiredObject(root, "contexts")),
          readPackagePolicy(requiredObject(root, "packagePolicy")),
          readQuality(requiredObject(root, "quality")));
    } catch (IOException | RuntimeException exception) {
      throw new LoaderException(
          "Failed to read compiled modpack profile " + path.toString().replace('\\', '/'),
          exception);
    }
  }

  private RuntimeConfigContract readConfig(JsonObject object) {
    if (object == null) {
      return RuntimeConfigContract.empty();
    }
    List<RuntimeConfigModPlan> mods = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "mods")) {
      JsonObject mod = element.getAsJsonObject();
      List<RuntimeConfigEntryPlan> entries = new ArrayList<>();
      for (JsonElement entryElement : requiredArray(mod, "entries")) {
        JsonObject entry = entryElement.getAsJsonObject();
        entries.add(
            new RuntimeConfigEntryPlan(
                requiredString(entry, "key"),
                requiredString(entry, "type"),
                requiredString(entry, "default"),
                optionalString(entry, "value"),
                requiredString(entry, "state"),
                requiredString(entry, "reason"),
                null,
                null,
                List.of(),
                null));
      }
      mods.add(
          new RuntimeConfigModPlan(
              requiredString(mod, "modId"),
              requiredString(mod, "path"),
              requiredBoolean(mod, "runtimeWrites"),
              requiredString(mod, "state"),
              entries,
              readStringArray(requiredArray(mod, "unknownKeys")),
              readConfigSummary(requiredObject(mod, "summary")),
              null));
    }
    return new RuntimeConfigContract(
        requiredInt(object, "contractVersion"),
        requiredString(object, "scope"),
        requiredString(object, "format"),
        mods,
        readConfigSummary(requiredObject(object, "summary")));
  }

  private RuntimeClosureContract readRuntimeClosure(JsonObject object) {
    if (object == null) {
      return RuntimeClosureContract.empty();
    }
    List<RuntimeClosureSurface> surfaces = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "surfaces")) {
      JsonObject surface = element.getAsJsonObject();
      surfaces.add(
          new RuntimeClosureSurface(
              requiredString(surface, "id"),
              requiredString(surface, "state"),
              optionalString(surface, "owner"),
              optionalString(surface, "capability"),
              optionalString(surface, "apiClass"),
              optionalString(surface, "profileSection"),
              optionalString(surface, "note")));
    }

    List<RuntimeClosureGate> gates = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "gates")) {
      JsonObject gate = element.getAsJsonObject();
      gates.add(
          new RuntimeClosureGate(
              requiredInt(gate, "order"),
              requiredString(gate, "id"),
              requiredString(gate, "phase"),
              requiredBoolean(gate, "beforeClassloading"),
              requiredString(gate, "fatalCondition"),
              requiredString(gate, "note")));
    }

    JsonObject boundary = requiredObject(object, "loaderApiBoundary");
    JsonObject summary = requiredObject(object, "summary");
    return new RuntimeClosureContract(
        requiredInt(object, "contractVersion"),
        requiredString(object, "arcStatus"),
        requiredString(object, "scope"),
        requiredString(object, "targetModel"),
        requiredString(object, "runtimeExecutionIsolationMode"),
        requiredBoolean(object, "sandboxed"),
        requiredString(object, "sandboxClaim"),
        surfaces,
        gates,
        new RuntimeClosureLoaderApiBoundary(
            requiredString(boundary, "status"),
            requiredString(boundary, "nextArc"),
            readStringArray(requiredArray(boundary, "stableCandidates")),
            readStringArray(requiredArray(boundary, "deferredReview")),
            readStringArray(requiredArray(boundary, "internalPackagesExcluded"))),
        new RuntimeClosureSummary(
            requiredInt(summary, "implemented"),
            requiredInt(summary, "unavailable"),
            requiredInt(summary, "visibilityOnly"),
            requiredInt(summary, "gates"),
            requiredInt(summary, "stableApiCandidates"),
            requiredInt(summary, "deferredApiReview")));
  }

  private CompiledModpackProfile.Cache readCache(JsonObject object) {
    return new CompiledModpackProfile.Cache(requiredString(object, "status"), requiredString(object, "reason"));
  }

  private CompiledModpackProfile.Loader readLoader(JsonObject object) {
    return new CompiledModpackProfile.Loader(requiredString(object, "id"), requiredString(object, "version"));
  }

  private CompiledModpackProfile.Game readGame(JsonObject object) {
    return new CompiledModpackProfile.Game(
        requiredString(object, "id"),
        requiredString(object, "version"),
        requiredString(object, "side"));
  }

  private CompiledModpackProfile.Metadata readMetadata(JsonObject object) {
    List<Integer> schemaVersions = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "schemaVersions")) {
      schemaVersions.add(element.getAsInt());
    }
    return new CompiledModpackProfile.Metadata(schemaVersions);
  }

  private List<CompiledModpackProfile.Mod> readMods(JsonArray array) {
    List<CompiledModpackProfile.Mod> mods = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject object = element.getAsJsonObject();
      mods.add(
          new CompiledModpackProfile.Mod(
              requiredString(object, "id"),
              requiredString(object, "version"),
              requiredString(object, "path"),
              requiredString(object, "hash")));
    }
    return List.copyOf(mods);
  }

  private List<String> readStringArray(JsonArray array) {
    List<String> values = new ArrayList<>();
    for (JsonElement element : array) {
      values.add(element.getAsString());
    }
    return List.copyOf(values);
  }

  private List<CompiledModpackProfile.ClasspathEntry> readClasspath(JsonArray array) {
    List<CompiledModpackProfile.ClasspathEntry> entries = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject object = element.getAsJsonObject();
      entries.add(
          new CompiledModpackProfile.ClasspathEntry(
              requiredString(object, "path"), requiredString(object, "owner")));
    }
    return List.copyOf(entries);
  }

  private CompiledModpackProfile.Ownership readOwnership(JsonObject object) {
    JsonObject classes = requiredObject(object, "classes");
    JsonObject packages = requiredObject(object, "packages");
    JsonObject resources = requiredObject(object, "resources");
    return new CompiledModpackProfile.Ownership(
        new CompiledModpackProfile.Count(requiredInt(classes, "count")),
        new CompiledModpackProfile.Count(requiredInt(packages, "count")),
        new CompiledModpackProfile.Resources(requiredInt(resources, "duplicates")));
  }

  private CompiledModpackProfile.Lockfile readLockfile(JsonObject object) {
    return new CompiledModpackProfile.Lockfile(
        requiredString(object, "mode"),
        optionalString(object, "action"),
        requiredString(object, "path"),
        requiredString(object, "fingerprint"));
  }

  private RuntimeServiceContract readServices(JsonObject object) {
    if (object == null) {
      return RuntimeServiceContract.empty();
    }
    List<RuntimeServiceModPlan> mods = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "mods")) {
      JsonObject mod = element.getAsJsonObject();
      List<RuntimeServiceProviderPlan> provides = new ArrayList<>();
      for (JsonElement providerElement : requiredArray(mod, "provides")) {
        JsonObject provider = providerElement.getAsJsonObject();
        provides.add(
            new RuntimeServiceProviderPlan(
                requiredString(provider, "id"),
                requiredString(provider, "type"),
                requiredString(provider, "implementation"),
                requiredString(provider, "state"),
                requiredString(provider, "reason")));
      }
      List<RuntimeServiceConsumerPlan> consumes = new ArrayList<>();
      for (JsonElement consumerElement : requiredArray(mod, "consumes")) {
        JsonObject consumer = consumerElement.getAsJsonObject();
        consumes.add(
            new RuntimeServiceConsumerPlan(
                requiredString(consumer, "id"),
                requiredString(consumer, "type"),
                requiredBoolean(consumer, "required"),
                requiredString(consumer, "state"),
                optionalString(consumer, "providerModId"),
                requiredString(consumer, "reason")));
      }
      mods.add(new RuntimeServiceModPlan(requiredString(mod, "modId"), provides, consumes));
    }

    List<RuntimeServiceBinding> bindings = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "bindings")) {
      JsonObject binding = element.getAsJsonObject();
      bindings.add(
          new RuntimeServiceBinding(
              requiredString(binding, "id"),
              requiredString(binding, "consumerModId"),
              optionalString(binding, "providerModId"),
              requiredString(binding, "type"),
              optionalString(binding, "implementation"),
              requiredBoolean(binding, "required"),
              requiredString(binding, "state")));
    }

    JsonObject summary = requiredObject(object, "summary");
    return new RuntimeServiceContract(
        requiredInt(object, "contractVersion"),
        requiredString(object, "scope"),
        requiredString(object, "providerInstantiation"),
        mods,
        bindings,
        new RuntimeServiceSummary(
            requiredInt(summary, "providers"),
            requiredInt(summary, "consumers"),
            requiredInt(summary, "bindings"),
            requiredInt(summary, "availableProviders"),
            requiredInt(summary, "conflictingProviders"),
            requiredInt(summary, "missingImplementations"),
            requiredInt(summary, "implementationOwnershipViolations"),
            requiredInt(summary, "requiredUnbound"),
            requiredInt(summary, "optionalUnbound"),
            requiredInt(summary, "typeMismatches"),
            requiredInt(summary, "fatalCount"),
            requiredInt(summary, "warningCount")));
  }

  private RuntimeCapabilityPlan readPermissions(JsonObject object) {
    List<RuntimeCapabilityModPlan> mods = new ArrayList<>();
    boolean runtimeTwoShape = object.has("catalogVersion");
    for (JsonElement element : requiredArray(object, "mods")) {
      JsonObject mod = element.getAsJsonObject();
      mods.add(
          new RuntimeCapabilityModPlan(
              requiredString(mod, "modId"),
              readStringArray(requiredArray(mod, "requested")),
              runtimeTwoShape ? readGrants(requiredArray(mod, "grants")) : List.of(),
              runtimeTwoShape
                  ? readCapabilitySummary(requiredObject(mod, "summary"))
                  : RuntimeCapabilitySummary.empty()));
    }
    if (!runtimeTwoShape) {
      return new RuntimeCapabilityPlan(0, null, null, false, mods, RuntimeCapabilitySummary.empty());
    }
    return new RuntimeCapabilityPlan(
        requiredInt(object, "catalogVersion"),
        requiredString(object, "scope"),
        requiredString(object, "runtimeExecutionIsolationMode"),
        requiredBoolean(object, "sandboxed"),
        mods,
        readCapabilitySummary(requiredObject(object, "summary")));
  }

  private List<RuntimeCapabilityGrant> readGrants(JsonArray array) {
    List<RuntimeCapabilityGrant> grants = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject grant = element.getAsJsonObject();
      grants.add(
          new RuntimeCapabilityGrant(
              requiredString(grant, "capability"),
              requiredString(grant, "state"),
              readStringArray(requiredArray(grant, "sources")),
              requiredString(grant, "reason"),
              requiredString(grant, "controls"),
              optionalString(grant, "fix")));
    }
    return List.copyOf(grants);
  }

  private RuntimeCapabilitySummary readCapabilitySummary(JsonObject object) {
    return new RuntimeCapabilitySummary(
        requiredInt(object, "granted"),
        requiredInt(object, "denied"),
        requiredInt(object, "unavailable"),
        requiredInt(object, "unknown"),
        requiredInt(object, "visibilityOnly"));
  }

  private CompiledModpackProfile.Lifecycle readLifecycle(JsonObject object) {
    List<String> phaseOrder = readStringArray(requiredArray(object, "phaseOrder"));
    List<CompiledModpackProfile.LifecycleHandler> handlers = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "handlers")) {
      JsonObject handler = element.getAsJsonObject();
      handlers.add(
          new CompiledModpackProfile.LifecycleHandler(
              requiredString(handler, "phase"),
              requiredString(handler, "modId"),
              requiredString(handler, "ownerModId"),
              requiredString(handler, "kind"),
              requiredString(handler, "className"),
              requiredString(handler, "methodName"),
              optionalString(handler, "interfaceName"),
              requiredString(handler, "jarPath"),
              requiredString(handler, "jarHash")));
    }
    return new CompiledModpackProfile.Lifecycle(phaseOrder, handlers);
  }

  private CompiledModpackProfile.Contexts readContexts(JsonObject object) {
    List<CompiledModpackProfile.ModContextPlan> contexts = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "mods")) {
      JsonObject context = element.getAsJsonObject();
      JsonObject storage = requiredObject(context, "storage");
      contexts.add(
          new CompiledModpackProfile.ModContextPlan(
              requiredString(context, "modId"),
              new CompiledModpackProfile.Storage(
                  requiredBoolean(storage, "config"),
                  requiredBoolean(storage, "data"),
                  requiredBoolean(storage, "cache"),
                  requiredBoolean(storage, "generated")),
              requiredString(context, "configDirectory"),
              requiredString(context, "dataDirectory"),
              requiredString(context, "cacheDirectory"),
              requiredString(context, "generatedDirectory")));
    }
    return new CompiledModpackProfile.Contexts(contexts);
  }

  private CompiledModpackProfile.PackagePolicy readPackagePolicy(JsonObject object) {
    List<CompiledModpackProfile.SplitPackage> splitPackages = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "splitPackages")) {
      JsonObject splitPackage = element.getAsJsonObject();
      splitPackages.add(
          new CompiledModpackProfile.SplitPackage(
              requiredString(splitPackage, "packageName"),
              readStringArray(requiredArray(splitPackage, "modIds"))));
    }

    List<CompiledModpackProfile.PackageOwner> packageOwners = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "packageOwners")) {
      JsonObject packageOwner = element.getAsJsonObject();
      packageOwners.add(
          new CompiledModpackProfile.PackageOwner(
              requiredString(packageOwner, "packageName"),
              readStringArray(requiredArray(packageOwner, "modIds"))));
    }

    List<ProtectedPackageViolation> fatalViolations = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "fatalViolations")) {
      JsonObject violation = element.getAsJsonObject();
      fatalViolations.add(
          new ProtectedPackageViolation(
              requiredString(violation, "modId"),
              requiredString(violation, "packageName"),
              requiredString(violation, "reason")));
    }

    return new CompiledModpackProfile.PackagePolicy(
        readStringArray(requiredArray(object, "protectedPackages")),
        splitPackages,
        readStringArray(requiredArray(object, "duplicateClasses")),
        packageOwners,
        fatalViolations);
  }

  private CompiledModpackProfile.Quality readQuality(JsonObject object) {
    return new CompiledModpackProfile.Quality(
        requiredInt(object, "score"),
        requiredInt(object, "fatalCount"),
        requiredInt(object, "warningCount"));
  }

  private RuntimeConfigSummary readConfigSummary(JsonObject object) {
    return new RuntimeConfigSummary(
        object.has("mods") ? requiredInt(object, "mods") : 0,
        requiredInt(object, "entries"),
        requiredInt(object, "valid"),
        requiredInt(object, "defaulted"),
        requiredInt(object, "invalid"),
        requiredInt(object, "unknownKeys"),
        object.has("storageNotGranted") ? requiredInt(object, "storageNotGranted") : 0,
        requiredInt(object, "fatalCount"),
        requiredInt(object, "warningCount"));
  }

  private JsonObject requiredObject(JsonObject object, String key) {
    return object.getAsJsonObject(key);
  }

  private JsonArray requiredArray(JsonObject object, String key) {
    return object.getAsJsonArray(key);
  }

  private String requiredString(JsonObject object, String key) {
    return object.get(key).getAsString();
  }

  private String optionalString(JsonObject object, String key) {
    return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
  }

  private int requiredInt(JsonObject object, String key) {
    return object.get(key).getAsInt();
  }

  private boolean requiredBoolean(JsonObject object, String key) {
    return object.get(key).getAsBoolean();
  }
}
