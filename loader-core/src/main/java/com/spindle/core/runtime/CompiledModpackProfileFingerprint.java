package com.spindle.core.runtime;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.runtime.capability.RuntimeCapabilityGrant;
import com.spindle.core.runtime.capability.RuntimeCapabilityModPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilityPlan;
import com.spindle.core.runtime.capability.RuntimeCapabilitySummary;
import com.spindle.core.runtime.config.RuntimeConfigEntryPlan;
import com.spindle.core.runtime.config.RuntimeConfigModPlan;
import com.spindle.core.runtime.config.RuntimeConfigSummary;
import com.spindle.core.runtime.service.RuntimeServiceBinding;
import com.spindle.core.runtime.service.RuntimeServiceConsumerPlan;
import com.spindle.core.runtime.service.RuntimeServiceModPlan;
import com.spindle.core.runtime.service.RuntimeServiceProviderPlan;
import com.spindle.core.runtime.service.RuntimeServiceSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CompiledModpackProfileFingerprint {
  public String compute(CompiledModpackProfile profile) throws LoaderException {
    MessageDigest digest = createDigest();
    update(digest, "schemaVersion", Integer.toString(profile.schemaVersion()));
    update(digest, "profileKind", profile.profileKind());
    update(digest, "inputFingerprint", profile.inputFingerprint());
    update(digest, "runtimePolicyFingerprint", profile.runtimePolicyFingerprint());
    update(digest, "loader.id", profile.loader().id());
    update(digest, "loader.version", profile.loader().version());
    update(digest, "game.id", profile.game().id());
    update(digest, "game.version", profile.game().version());
    update(digest, "game.side", profile.game().side());

    for (Integer schemaVersion : profile.metadata().schemaVersions()) {
      update(digest, "metadata.schemaVersion", Integer.toString(schemaVersion));
    }

    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      update(digest, "mod.id", mod.id());
      update(digest, "mod.version", mod.version());
      update(digest, "mod.path", mod.path());
      update(digest, "mod.hash", mod.hash());
    }

    for (String modId : profile.resolvedOrder()) {
      update(digest, "resolvedOrder", modId);
    }

    for (CompiledModpackProfile.ClasspathEntry entry : profile.classpath()) {
      update(digest, "classpath.path", entry.path());
      update(digest, "classpath.owner", entry.owner());
    }

    update(digest, "ownership.classes.count", Integer.toString(profile.ownership().classes().count()));
    update(
        digest,
        "ownership.packages.count",
        Integer.toString(profile.ownership().packages().count()));
    update(
        digest,
        "ownership.resources.duplicates",
        Integer.toString(profile.ownership().resources().duplicates()));
    update(digest, "lockfile.mode", profile.lockfile().mode());
    update(digest, "lockfile.path", profile.lockfile().path());
    update(digest, "lockfile.fingerprint", profile.lockfile().fingerprint());

    RuntimeCapabilityPlan permissions = profile.permissions();
    update(digest, "permissions.catalogVersion", Integer.toString(permissions.catalogVersion()));
    update(digest, "permissions.scope", permissions.scope());
    update(
        digest,
        "permissions.runtimeExecutionIsolationMode",
        permissions.runtimeExecutionIsolationMode());
    update(digest, "permissions.sandboxed", Boolean.toString(permissions.sandboxed()));
    for (RuntimeCapabilityModPlan modPermissions : permissions.mods()) {
      update(digest, "permissions.modId", modPermissions.modId());
      for (String requested : modPermissions.requested()) {
        update(digest, "permissions.requested", requested);
      }
      for (RuntimeCapabilityGrant grant : modPermissions.grants()) {
        update(digest, "permissions.grant.capability", grant.capability());
        update(digest, "permissions.grant.state", grant.state());
        for (String source : grant.sources()) {
          update(digest, "permissions.grant.source", source);
        }
        update(digest, "permissions.grant.reason", grant.reason());
        update(digest, "permissions.grant.controls", grant.controls());
        update(digest, "permissions.grant.fix", grant.fix());
      }
      updateCapabilitySummary(digest, "permissions.mod.summary", modPermissions.summary());
    }
    updateCapabilitySummary(digest, "permissions.summary", permissions.summary());

    update(digest, "config.contractVersion", Integer.toString(profile.config().contractVersion()));
    update(digest, "config.scope", profile.config().scope());
    update(digest, "config.format", profile.config().format());
    for (RuntimeConfigModPlan modConfig : profile.config().mods()) {
      update(digest, "config.modId", modConfig.modId());
      update(digest, "config.path", modConfig.path());
      update(digest, "config.runtimeWrites", Boolean.toString(modConfig.runtimeWrites()));
      update(digest, "config.state", modConfig.state());
      for (RuntimeConfigEntryPlan entry : modConfig.entries()) {
        update(digest, "config.entry.key", entry.key());
        update(digest, "config.entry.type", entry.type());
        update(digest, "config.entry.default", entry.defaultValue());
        update(digest, "config.entry.value", entry.value());
        update(digest, "config.entry.state", entry.state());
        update(digest, "config.entry.reason", entry.reason());
      }
      for (String unknownKey : modConfig.unknownKeys()) {
        update(digest, "config.unknownKey", unknownKey);
      }
      updateConfigSummary(digest, "config.mod.summary", modConfig.summary());
    }
    updateConfigSummary(digest, "config.summary", profile.config().summary());

    update(digest, "services.contractVersion", Integer.toString(profile.services().contractVersion()));
    update(digest, "services.scope", profile.services().scope());
    update(digest, "services.providerInstantiation", profile.services().providerInstantiation());
    for (RuntimeServiceModPlan modServices : profile.services().mods()) {
      update(digest, "services.modId", modServices.modId());
      for (RuntimeServiceProviderPlan provider : modServices.provides()) {
        update(digest, "services.provider.id", provider.id());
        update(digest, "services.provider.type", provider.type());
        update(digest, "services.provider.implementation", provider.implementation());
        update(digest, "services.provider.state", provider.state());
        update(digest, "services.provider.reason", provider.reason());
      }
      for (RuntimeServiceConsumerPlan consumer : modServices.consumes()) {
        update(digest, "services.consumer.id", consumer.id());
        update(digest, "services.consumer.type", consumer.type());
        update(digest, "services.consumer.required", Boolean.toString(consumer.required()));
        update(digest, "services.consumer.state", consumer.state());
        update(digest, "services.consumer.providerModId", consumer.providerModId());
        update(digest, "services.consumer.reason", consumer.reason());
      }
    }
    for (RuntimeServiceBinding binding : profile.services().bindings()) {
      update(digest, "services.binding.id", binding.id());
      update(digest, "services.binding.consumerModId", binding.consumerModId());
      update(digest, "services.binding.providerModId", binding.providerModId());
      update(digest, "services.binding.type", binding.type());
      update(digest, "services.binding.implementation", binding.implementation());
      update(digest, "services.binding.required", Boolean.toString(binding.required()));
      update(digest, "services.binding.state", binding.state());
    }
    updateServiceSummary(digest, "services.summary", profile.services().summary());

    for (String phase : profile.lifecycle().phaseOrder()) {
      update(digest, "lifecycle.phaseOrder", phase);
    }
    for (CompiledModpackProfile.LifecycleHandler handler : profile.lifecycle().handlers()) {
      update(digest, "lifecycle.handler.phase", handler.phase());
      update(digest, "lifecycle.handler.modId", handler.modId());
      update(digest, "lifecycle.handler.ownerModId", handler.ownerModId());
      update(digest, "lifecycle.handler.kind", handler.kind());
      update(digest, "lifecycle.handler.className", handler.className());
      update(digest, "lifecycle.handler.methodName", handler.methodName());
      update(digest, "lifecycle.handler.interfaceName", handler.interfaceName());
      update(digest, "lifecycle.handler.jarPath", handler.jarPath());
      update(digest, "lifecycle.handler.jarHash", handler.jarHash());
    }

    for (CompiledModpackProfile.ModContextPlan context : profile.contexts().mods()) {
      update(digest, "context.modId", context.modId());
      update(digest, "context.storage.config", Boolean.toString(context.storage().config()));
      update(digest, "context.storage.data", Boolean.toString(context.storage().data()));
      update(digest, "context.storage.cache", Boolean.toString(context.storage().cache()));
      update(
          digest, "context.storage.generated", Boolean.toString(context.storage().generated()));
      update(digest, "context.configDirectory", context.configDirectory());
      update(digest, "context.dataDirectory", context.dataDirectory());
      update(digest, "context.cacheDirectory", context.cacheDirectory());
      update(digest, "context.generatedDirectory", context.generatedDirectory());
    }

    for (String protectedPackage : profile.packagePolicy().protectedPackages()) {
      update(digest, "packagePolicy.protectedPackage", protectedPackage);
    }
    for (CompiledModpackProfile.SplitPackage splitPackage : profile.packagePolicy().splitPackages()) {
      update(digest, "packagePolicy.splitPackage.name", splitPackage.packageName());
      for (String modId : splitPackage.modIds()) {
        update(digest, "packagePolicy.splitPackage.modId", modId);
      }
    }
    for (String duplicateClass : profile.packagePolicy().duplicateClasses()) {
      update(digest, "packagePolicy.duplicateClass", duplicateClass);
    }
    for (CompiledModpackProfile.PackageOwner owner : profile.packagePolicy().packageOwners()) {
      update(digest, "packagePolicy.packageOwner.name", owner.packageName());
      for (String modId : owner.modIds()) {
        update(digest, "packagePolicy.packageOwner.modId", modId);
      }
    }
    for (ProtectedPackageViolation violation : profile.packagePolicy().fatalViolations()) {
      update(digest, "packagePolicy.fatalViolation.modId", violation.modId());
      update(digest, "packagePolicy.fatalViolation.packageName", violation.packageName());
      update(digest, "packagePolicy.fatalViolation.reason", violation.reason());
    }

    update(digest, "quality.score", Integer.toString(profile.quality().score()));
    update(digest, "quality.fatalCount", Integer.toString(profile.quality().fatalCount()));
    update(digest, "quality.warningCount", Integer.toString(profile.quality().warningCount()));
    return HexFormat.of().formatHex(digest.digest());
  }

  public String computeInputFingerprint(
      LaunchContext context, ModpackPlanningResult planningResult, String gameSide)
      throws LoaderException {
    MessageDigest digest = createDigest();
    update(digest, "profileSchemaVersion", Integer.toString(CompiledModpackProfile.SCHEMA_VERSION));
    update(digest, "loaderVersion", context.loaderVersion());
    update(digest, "javaMajorVersion", Integer.toString(context.javaMajorVersion()));
    update(digest, "gameProviderId", planningResult.frozenModGraph().gameProviderId());
    update(digest, "gameProviderVersion", planningResult.frozenModGraph().gameProviderVersion());
    update(digest, "gameSide", gameSide);
    update(digest, "lockfileFingerprint", fromFile(planningResult.lockfilePath()));
    update(digest, "strictResources", Boolean.toString(context.strictResources()));
    update(digest, "strictPackages", Boolean.toString(context.strictPackages()));
    update(
        digest,
        "runtimeCapabilityCatalogVersion",
        Integer.toString(com.spindle.core.runtime.capability.RuntimeCapabilityCatalog.CATALOG_VERSION));
    update(
        digest,
        "runtimeServiceContractVersion",
        Integer.toString(
            com.spindle.core.runtime.service.RuntimeServiceContract.CONTRACT_VERSION));
    update(
        digest,
        "runtimeConfigContractVersion",
        Integer.toString(com.spindle.core.runtime.config.RuntimeConfigContract.CONTRACT_VERSION));

    for (var mod : planningResult.resolvedMods().mods()) {
      update(digest, "mod.id", mod.id());
      update(digest, "mod.version", mod.version());
      update(digest, "mod.path", mod.normalizedRelativePath());
      update(digest, "mod.hash", mod.sha256());
      update(digest, "mod.schema", Integer.toString(mod.metadataSchema()));
      update(digest, "mod.config.runtimeWrites", Boolean.toString(mod.config().runtimeWrites()));
      for (var entry : mod.config().entries()) {
        update(digest, "mod.config.key", entry.key());
        update(digest, "mod.config.type", entry.type());
        update(digest, "mod.config.default", entry.defaultValue());
        update(digest, "mod.config.min", entry.min());
        update(digest, "mod.config.max", entry.max());
        for (String allowed : entry.allowed()) {
          update(digest, "mod.config.allowed", allowed);
        }
      }
      for (var provider : mod.services().provides()) {
        update(digest, "mod.services.provider.id", provider.id());
        update(digest, "mod.services.provider.type", provider.type());
        update(digest, "mod.services.provider.implementation", provider.implementation());
      }
      for (var consumer : mod.services().consumes()) {
        update(digest, "mod.services.consumer.id", consumer.id());
        update(digest, "mod.services.consumer.type", consumer.type());
        update(digest, "mod.services.consumer.required", Boolean.toString(consumer.required()));
      }
    }
    for (String modId :
        planningResult.resolvedMods().mods().stream().map(mod -> mod.id()).toList()) {
      update(digest, "resolvedOrder", modId);
    }
    for (Path path : planningResult.classpathPlan().modJars()) {
      update(digest, "classpath", relativize(context.workingDirectory(), path));
    }

    return HexFormat.of().formatHex(digest.digest());
  }

  public static String fromFile(Path path) throws LoaderException {
    try {
      return sha256(Files.readAllBytes(path));
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read compiled profile fingerprint input "
              + path.toString().replace('\\', '/'),
          exception);
    }
  }

  public static String sha256(byte[] bytes) throws LoaderException {
    return HexFormat.of().formatHex(createDigest().digest(bytes));
  }

  private static String relativize(Path workingDirectory, Path path) {
    try {
      return workingDirectory
          .toAbsolutePath()
          .normalize()
          .relativize(path.toAbsolutePath().normalize())
          .toString()
          .replace('\\', '/');
    } catch (IllegalArgumentException exception) {
      return path.toAbsolutePath().normalize().toString().replace('\\', '/');
    }
  }

  static MessageDigest createDigest() throws LoaderException {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      throw new LoaderException("SHA-256 algorithm unavailable", exception);
    }
  }

  private static void update(MessageDigest digest, String key, String value) {
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '=');
    digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '\n');
  }

  private static void updateCapabilitySummary(
      MessageDigest digest, String keyPrefix, RuntimeCapabilitySummary summary) {
    update(digest, keyPrefix + ".granted", Integer.toString(summary.granted()));
    update(digest, keyPrefix + ".denied", Integer.toString(summary.denied()));
    update(digest, keyPrefix + ".unavailable", Integer.toString(summary.unavailable()));
    update(digest, keyPrefix + ".unknown", Integer.toString(summary.unknown()));
    update(
        digest, keyPrefix + ".visibilityOnly", Integer.toString(summary.visibilityOnly()));
  }

  private static void updateServiceSummary(
      MessageDigest digest, String keyPrefix, RuntimeServiceSummary summary) {
    update(digest, keyPrefix + ".providers", Integer.toString(summary.providers()));
    update(digest, keyPrefix + ".consumers", Integer.toString(summary.consumers()));
    update(digest, keyPrefix + ".bindings", Integer.toString(summary.bindings()));
    update(
        digest,
        keyPrefix + ".availableProviders",
        Integer.toString(summary.availableProviders()));
    update(
        digest,
        keyPrefix + ".conflictingProviders",
        Integer.toString(summary.conflictingProviders()));
    update(
        digest,
        keyPrefix + ".missingImplementations",
        Integer.toString(summary.missingImplementations()));
    update(
        digest,
        keyPrefix + ".implementationOwnershipViolations",
        Integer.toString(summary.implementationOwnershipViolations()));
    update(
        digest, keyPrefix + ".requiredUnbound", Integer.toString(summary.requiredUnbound()));
    update(
        digest, keyPrefix + ".optionalUnbound", Integer.toString(summary.optionalUnbound()));
    update(
        digest, keyPrefix + ".typeMismatches", Integer.toString(summary.typeMismatches()));
    update(digest, keyPrefix + ".fatalCount", Integer.toString(summary.fatalCount()));
    update(digest, keyPrefix + ".warningCount", Integer.toString(summary.warningCount()));
  }

  private static void updateConfigSummary(
      MessageDigest digest, String keyPrefix, RuntimeConfigSummary summary) {
    update(digest, keyPrefix + ".mods", Integer.toString(summary.mods()));
    update(digest, keyPrefix + ".entries", Integer.toString(summary.entries()));
    update(digest, keyPrefix + ".valid", Integer.toString(summary.valid()));
    update(digest, keyPrefix + ".defaulted", Integer.toString(summary.defaulted()));
    update(digest, keyPrefix + ".invalid", Integer.toString(summary.invalid()));
    update(digest, keyPrefix + ".unknownKeys", Integer.toString(summary.unknownKeys()));
    update(
        digest,
        keyPrefix + ".storageNotGranted",
        Integer.toString(summary.storageNotGranted()));
    update(digest, keyPrefix + ".fatalCount", Integer.toString(summary.fatalCount()));
    update(digest, keyPrefix + ".warningCount", Integer.toString(summary.warningCount()));
  }
}
