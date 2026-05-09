package com.spindle.core.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.capability.RuntimeCapabilityGrant;
import com.spindle.core.runtime.capability.RuntimeCapabilityModPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilitySummary;
import com.spindle.core.runtime.config.RuntimeConfigContract;
import com.spindle.core.runtime.config.RuntimeConfigEntryPlan;
import com.spindle.core.runtime.config.RuntimeConfigModPlan;
import com.spindle.core.runtime.config.RuntimeConfigSummary;
import com.spindle.core.runtime.service.RuntimeServiceBinding;
import com.spindle.core.runtime.service.RuntimeServiceConsumerPlan;
import com.spindle.core.runtime.service.RuntimeServiceModPlan;
import com.spindle.core.runtime.service.RuntimeServiceProviderPlan;
import com.spindle.core.runtime.service.RuntimeServiceSummary;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CompiledModpackProfileWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public CompiledModpackProfileResult write(Path outputPath, CompiledModpackProfile profile)
      throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", profile.schemaVersion());
    root.addProperty("profileKind", profile.profileKind());
    root.addProperty("fingerprint", profile.fingerprint());
    root.addProperty("inputFingerprint", profile.inputFingerprint());
    root.addProperty("runtimePolicyFingerprint", profile.runtimePolicyFingerprint());

    JsonObject cache = new JsonObject();
    cache.addProperty("status", profile.cache().status());
    cache.addProperty("reason", profile.cache().reason());
    root.add("cache", cache);

    JsonObject loader = new JsonObject();
    loader.addProperty("id", profile.loader().id());
    loader.addProperty("version", profile.loader().version());
    root.add("loader", loader);

    JsonObject game = new JsonObject();
    game.addProperty("id", profile.game().id());
    game.addProperty("version", profile.game().version());
    game.addProperty("side", profile.game().side());
    root.add("game", game);

    JsonObject metadata = new JsonObject();
    JsonArray schemaVersions = new JsonArray();
    for (Integer schemaVersion : profile.metadata().schemaVersions()) {
      schemaVersions.add(schemaVersion);
    }
    metadata.add("schemaVersions", schemaVersions);
    root.add("metadata", metadata);

    JsonArray mods = new JsonArray();
    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      JsonObject modObject = new JsonObject();
      modObject.addProperty("id", mod.id());
      modObject.addProperty("version", mod.version());
      modObject.addProperty("path", mod.path());
      modObject.addProperty("hash", mod.hash());
      mods.add(modObject);
    }
    root.add("mods", mods);

    JsonArray resolvedOrder = new JsonArray();
    for (String modId : profile.resolvedOrder()) {
      resolvedOrder.add(modId);
    }
    root.add("resolvedOrder", resolvedOrder);

    JsonArray classpath = new JsonArray();
    for (CompiledModpackProfile.ClasspathEntry entry : profile.classpath()) {
      JsonObject classpathObject = new JsonObject();
      classpathObject.addProperty("path", entry.path());
      classpathObject.addProperty("owner", entry.owner());
      classpath.add(classpathObject);
    }
    root.add("classpath", classpath);

    JsonObject ownership = new JsonObject();
    JsonObject classes = new JsonObject();
    classes.addProperty("count", profile.ownership().classes().count());
    ownership.add("classes", classes);
    JsonObject packages = new JsonObject();
    packages.addProperty("count", profile.ownership().packages().count());
    ownership.add("packages", packages);
    JsonObject resources = new JsonObject();
    resources.addProperty("duplicates", profile.ownership().resources().duplicates());
    ownership.add("resources", resources);
    root.add("ownership", ownership);

    JsonObject lockfile = new JsonObject();
    lockfile.addProperty("mode", profile.lockfile().mode());
    if (profile.lockfile().action() != null) {
      lockfile.addProperty("action", profile.lockfile().action());
    }
    lockfile.addProperty("path", profile.lockfile().path());
    lockfile.addProperty("fingerprint", profile.lockfile().fingerprint());
    root.add("lockfile", lockfile);

    JsonObject permissions = new JsonObject();
    permissions.addProperty("catalogVersion", profile.permissions().catalogVersion());
    permissions.addProperty("scope", profile.permissions().scope());
    permissions.addProperty(
        "runtimeExecutionIsolationMode", profile.permissions().runtimeExecutionIsolationMode());
    permissions.addProperty("sandboxed", profile.permissions().sandboxed());
    JsonArray permissionMods = new JsonArray();
    for (RuntimeCapabilityModPlan mod : profile.permissions().mods()) {
      JsonObject modObject = new JsonObject();
      modObject.addProperty("modId", mod.modId());
      JsonArray requested = new JsonArray();
      for (String permission : mod.requested()) {
        requested.add(permission);
      }
      modObject.add("requested", requested);
      JsonArray grants = new JsonArray();
      for (RuntimeCapabilityGrant grant : mod.grants()) {
        JsonObject grantObject = new JsonObject();
        grantObject.addProperty("capability", grant.capability());
        grantObject.addProperty("state", grant.state());
        JsonArray sources = new JsonArray();
        for (String source : grant.sources()) {
          sources.add(source);
        }
        grantObject.add("sources", sources);
        grantObject.addProperty("reason", grant.reason());
        grantObject.addProperty("controls", grant.controls());
        if (grant.fix() == null) {
          grantObject.add("fix", JsonNull.INSTANCE);
        } else {
          grantObject.addProperty("fix", grant.fix());
        }
        grants.add(grantObject);
      }
      modObject.add("grants", grants);
      modObject.add("summary", capabilitySummary(mod.summary()));
      permissionMods.add(modObject);
    }
    permissions.add("mods", permissionMods);
    permissions.add("summary", capabilitySummary(profile.permissions().summary()));
    root.add("permissions", permissions);

    root.add("config", configContract(profile.config()));

    JsonObject services = new JsonObject();
    services.addProperty("contractVersion", profile.services().contractVersion());
    services.addProperty("scope", profile.services().scope());
    services.addProperty("providerInstantiation", profile.services().providerInstantiation());
    JsonArray serviceMods = new JsonArray();
    for (RuntimeServiceModPlan modPlan : profile.services().mods()) {
      JsonObject modObject = new JsonObject();
      modObject.addProperty("modId", modPlan.modId());
      JsonArray provides = new JsonArray();
      for (RuntimeServiceProviderPlan provider : modPlan.provides()) {
        JsonObject providerObject = new JsonObject();
        providerObject.addProperty("id", provider.id());
        providerObject.addProperty("type", provider.type());
        providerObject.addProperty("implementation", provider.implementation());
        providerObject.addProperty("state", provider.state());
        providerObject.addProperty("reason", provider.reason());
        provides.add(providerObject);
      }
      modObject.add("provides", provides);
      JsonArray consumes = new JsonArray();
      for (RuntimeServiceConsumerPlan consumer : modPlan.consumes()) {
        JsonObject consumerObject = new JsonObject();
        consumerObject.addProperty("id", consumer.id());
        consumerObject.addProperty("type", consumer.type());
        consumerObject.addProperty("required", consumer.required());
        consumerObject.addProperty("state", consumer.state());
        if (consumer.providerModId() == null) {
          consumerObject.add("providerModId", JsonNull.INSTANCE);
        } else {
          consumerObject.addProperty("providerModId", consumer.providerModId());
        }
        consumerObject.addProperty("reason", consumer.reason());
        consumes.add(consumerObject);
      }
      modObject.add("consumes", consumes);
      serviceMods.add(modObject);
    }
    services.add("mods", serviceMods);
    JsonArray bindings = new JsonArray();
    for (RuntimeServiceBinding binding : profile.services().bindings()) {
      JsonObject bindingObject = new JsonObject();
      bindingObject.addProperty("id", binding.id());
      bindingObject.addProperty("consumerModId", binding.consumerModId());
      if (binding.providerModId() == null) {
        bindingObject.add("providerModId", JsonNull.INSTANCE);
      } else {
        bindingObject.addProperty("providerModId", binding.providerModId());
      }
      bindingObject.addProperty("type", binding.type());
      if (binding.implementation() == null) {
        bindingObject.add("implementation", JsonNull.INSTANCE);
      } else {
        bindingObject.addProperty("implementation", binding.implementation());
      }
      bindingObject.addProperty("required", binding.required());
      bindingObject.addProperty("state", binding.state());
      bindings.add(bindingObject);
    }
    services.add("bindings", bindings);
    services.add("summary", serviceSummary(profile.services().summary()));
    root.add("services", services);

    JsonObject lifecycle = new JsonObject();
    JsonArray phaseOrder = new JsonArray();
    for (String phase : profile.lifecycle().phaseOrder()) {
      phaseOrder.add(phase);
    }
    lifecycle.add("phaseOrder", phaseOrder);
    JsonArray handlers = new JsonArray();
    for (CompiledModpackProfile.LifecycleHandler handler : profile.lifecycle().handlers()) {
      JsonObject handlerObject = new JsonObject();
      handlerObject.addProperty("phase", handler.phase());
      handlerObject.addProperty("modId", handler.modId());
      handlerObject.addProperty("ownerModId", handler.ownerModId());
      handlerObject.addProperty("kind", handler.kind());
      handlerObject.addProperty("className", handler.className());
      handlerObject.addProperty("methodName", handler.methodName());
      if (handler.interfaceName() != null) {
        handlerObject.addProperty("interfaceName", handler.interfaceName());
      }
      handlerObject.addProperty("jarPath", handler.jarPath());
      handlerObject.addProperty("jarHash", handler.jarHash());
      handlers.add(handlerObject);
    }
    lifecycle.add("handlers", handlers);
    root.add("lifecycle", lifecycle);

    JsonObject contexts = new JsonObject();
    JsonArray contextMods = new JsonArray();
    for (CompiledModpackProfile.ModContextPlan context : profile.contexts().mods()) {
      JsonObject contextObject = new JsonObject();
      contextObject.addProperty("modId", context.modId());
      JsonObject storage = new JsonObject();
      storage.addProperty("config", context.storage().config());
      storage.addProperty("data", context.storage().data());
      storage.addProperty("cache", context.storage().cache());
      storage.addProperty("generated", context.storage().generated());
      contextObject.add("storage", storage);
      contextObject.addProperty("configDirectory", context.configDirectory());
      contextObject.addProperty("dataDirectory", context.dataDirectory());
      contextObject.addProperty("cacheDirectory", context.cacheDirectory());
      contextObject.addProperty("generatedDirectory", context.generatedDirectory());
      contextMods.add(contextObject);
    }
    contexts.add("mods", contextMods);
    root.add("contexts", contexts);

    JsonObject packagePolicy = new JsonObject();
    JsonArray protectedPackages = new JsonArray();
    for (String protectedPackage : profile.packagePolicy().protectedPackages()) {
      protectedPackages.add(protectedPackage);
    }
    packagePolicy.add("protectedPackages", protectedPackages);
    JsonArray splitPackages = new JsonArray();
    for (CompiledModpackProfile.SplitPackage splitPackage : profile.packagePolicy().splitPackages()) {
      JsonObject splitPackageObject = new JsonObject();
      splitPackageObject.addProperty("packageName", splitPackage.packageName());
      JsonArray modIds = new JsonArray();
      for (String modId : splitPackage.modIds()) {
        modIds.add(modId);
      }
      splitPackageObject.add("modIds", modIds);
      splitPackages.add(splitPackageObject);
    }
    packagePolicy.add("splitPackages", splitPackages);
    JsonArray duplicateClasses = new JsonArray();
    for (String duplicateClass : profile.packagePolicy().duplicateClasses()) {
      duplicateClasses.add(duplicateClass);
    }
    packagePolicy.add("duplicateClasses", duplicateClasses);
    JsonArray packageOwners = new JsonArray();
    for (CompiledModpackProfile.PackageOwner owner : profile.packagePolicy().packageOwners()) {
      JsonObject ownerObject = new JsonObject();
      ownerObject.addProperty("packageName", owner.packageName());
      JsonArray modIds = new JsonArray();
      for (String modId : owner.modIds()) {
        modIds.add(modId);
      }
      ownerObject.add("modIds", modIds);
      packageOwners.add(ownerObject);
    }
    packagePolicy.add("packageOwners", packageOwners);
    JsonArray fatalViolations = new JsonArray();
    for (ProtectedPackageViolation violation : profile.packagePolicy().fatalViolations()) {
      JsonObject violationObject = new JsonObject();
      violationObject.addProperty("modId", violation.modId());
      violationObject.addProperty("packageName", violation.packageName());
      violationObject.addProperty("reason", violation.reason());
      fatalViolations.add(violationObject);
    }
    packagePolicy.add("fatalViolations", fatalViolations);
    root.add("packagePolicy", packagePolicy);

    JsonObject quality = new JsonObject();
    quality.addProperty("score", profile.quality().score());
    quality.addProperty("fatalCount", profile.quality().fatalCount());
    quality.addProperty("warningCount", profile.quality().warningCount());
    root.add("quality", quality);

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write compiled modpack profile " + outputPath.getFileName(), exception);
    }

    return new CompiledModpackProfileResult(
        outputPath, profile.schemaVersion(), profile.profileKind(), profile.fingerprint());
  }

  private JsonObject configContract(RuntimeConfigContract contract) {
    JsonObject config = new JsonObject();
    config.addProperty("contractVersion", contract.contractVersion());
    config.addProperty("scope", contract.scope());
    config.addProperty("format", contract.format());
    JsonArray mods = new JsonArray();
    for (RuntimeConfigModPlan modPlan : contract.mods()) {
      JsonObject mod = new JsonObject();
      mod.addProperty("modId", modPlan.modId());
      mod.addProperty("path", modPlan.path());
      mod.addProperty("runtimeWrites", modPlan.runtimeWrites());
      mod.addProperty("state", modPlan.state());
      JsonArray entries = new JsonArray();
      for (RuntimeConfigEntryPlan entry : modPlan.entries()) {
        JsonObject entryObject = new JsonObject();
        entryObject.addProperty("key", entry.key());
        entryObject.addProperty("type", entry.type());
        entryObject.addProperty("default", entry.defaultValue());
        if (entry.value() == null) {
          entryObject.add("value", JsonNull.INSTANCE);
        } else {
          entryObject.addProperty("value", entry.value());
        }
        entryObject.addProperty("state", entry.state());
        entryObject.addProperty("reason", entry.reason());
        entries.add(entryObject);
      }
      mod.add("entries", entries);
      JsonArray unknownKeys = new JsonArray();
      for (String unknownKey : modPlan.unknownKeys()) {
        unknownKeys.add(unknownKey);
      }
      mod.add("unknownKeys", unknownKeys);
      mod.add("summary", configSummary(modPlan.summary()));
      mods.add(mod);
    }
    config.add("mods", mods);
    config.add("summary", configSummary(contract.summary()));
    return config;
  }

  private JsonObject capabilitySummary(RuntimeCapabilitySummary summary) {
    JsonObject object = new JsonObject();
    object.addProperty("granted", summary.granted());
    object.addProperty("denied", summary.denied());
    object.addProperty("unavailable", summary.unavailable());
    object.addProperty("unknown", summary.unknown());
    object.addProperty("visibilityOnly", summary.visibilityOnly());
    return object;
  }

  private JsonObject serviceSummary(RuntimeServiceSummary summary) {
    JsonObject object = new JsonObject();
    object.addProperty("providers", summary.providers());
    object.addProperty("consumers", summary.consumers());
    object.addProperty("bindings", summary.bindings());
    object.addProperty("availableProviders", summary.availableProviders());
    object.addProperty("conflictingProviders", summary.conflictingProviders());
    object.addProperty("missingImplementations", summary.missingImplementations());
    object.addProperty(
        "implementationOwnershipViolations", summary.implementationOwnershipViolations());
    object.addProperty("requiredUnbound", summary.requiredUnbound());
    object.addProperty("optionalUnbound", summary.optionalUnbound());
    object.addProperty("typeMismatches", summary.typeMismatches());
    object.addProperty("fatalCount", summary.fatalCount());
    object.addProperty("warningCount", summary.warningCount());
    return object;
  }

  private JsonObject configSummary(RuntimeConfigSummary summary) {
    JsonObject object = new JsonObject();
    object.addProperty("mods", summary.mods());
    object.addProperty("entries", summary.entries());
    object.addProperty("valid", summary.valid());
    object.addProperty("defaulted", summary.defaulted());
    object.addProperty("invalid", summary.invalid());
    object.addProperty("unknownKeys", summary.unknownKeys());
    object.addProperty("storageNotGranted", summary.storageNotGranted());
    object.addProperty("fatalCount", summary.fatalCount());
    object.addProperty("warningCount", summary.warningCount());
    return object;
  }
}
