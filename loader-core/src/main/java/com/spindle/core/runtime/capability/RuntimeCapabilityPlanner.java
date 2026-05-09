package com.spindle.core.runtime.capability;

import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class RuntimeCapabilityPlanner {
  public RuntimeCapabilityPlan plan(ResolvedModSet resolvedMods) {
    List<RuntimeCapabilityModPlan> mods = new ArrayList<>(resolvedMods.mods().size());
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      mods.add(planMod(mod));
    }
    return new RuntimeCapabilityPlan(
        RuntimeCapabilityCatalog.CATALOG_VERSION,
        RuntimeCapabilityCatalog.SCOPE,
        RuntimeCapabilityCatalog.RUNTIME_EXECUTION_ISOLATION_MODE,
        RuntimeCapabilityCatalog.SANDBOXED,
        mods,
        null);
  }

  private RuntimeCapabilityModPlan planMod(ResolvedModSet.ResolvedMod mod) {
    Map<String, GrantBuilder> grants = new TreeMap<>();
    addGrantedStorageCapabilities(grants, mod.storage());
    for (String requestedCapability : mod.permissions()) {
      if (RuntimeCapabilityCatalog.isGrantableStorage(requestedCapability)) {
        addRequestedStorageCapability(grants, requestedCapability, mod.storage());
        continue;
      }
      if (RuntimeCapabilityCatalog.isUnavailable(requestedCapability)) {
        grants.put(
            requestedCapability,
            new GrantBuilder(
                requestedCapability,
                RuntimeCapabilityState.UNAVAILABLE,
                List.of("metadata.permissions"),
                "Runtime-2 recognizes "
                    + requestedCapability
                    + " but the corresponding Spindle API surface is not implemented yet.",
                "No Spindle API surface is granted in this runtime version.",
                "Keep the declaration for future planning, but do not rely on this capability yet."));
        continue;
      }
      if (RuntimeCapabilityCatalog.isVisibilityOnly(requestedCapability)) {
        grants.put(
            requestedCapability,
            new GrantBuilder(
                requestedCapability,
                RuntimeCapabilityState.VISIBILITY_ONLY,
                List.of("metadata.permissions"),
                "Runtime-2 records "
                    + requestedCapability
                    + " for review but does not enforce this broad Java behavior.",
                "No Java sandbox or process restriction is applied.",
                "Treat this as a disclosure signal only."));
        continue;
      }
      grants.put(
          requestedCapability,
          new GrantBuilder(
              requestedCapability,
              RuntimeCapabilityState.UNKNOWN,
              List.of("metadata.permissions"),
              "Runtime-2 does not recognize requested capability `" + requestedCapability + "`.",
              "No Spindle API surface is granted for unknown capabilities.",
              "Rename the capability to a Runtime-2 catalog entry or keep it as documentation only."));
    }
    List<RuntimeCapabilityGrant> compiledGrants =
        grants.values().stream().map(GrantBuilder::build).toList();
    return new RuntimeCapabilityModPlan(
        mod.id(), mod.permissions(), compiledGrants, RuntimeCapabilitySummary.fromGrants(compiledGrants));
  }

  private void addGrantedStorageCapabilities(
      Map<String, GrantBuilder> grants, ModMetadata.Storage storage) {
    if (storage.config()) {
      grants.put(
          RuntimeCapabilityCatalog.STORAGE_CONFIG,
          grantedStorageCapability(RuntimeCapabilityCatalog.STORAGE_CONFIG));
    }
    if (storage.data()) {
      grants.put(
          RuntimeCapabilityCatalog.STORAGE_DATA,
          grantedStorageCapability(RuntimeCapabilityCatalog.STORAGE_DATA));
    }
    if (storage.cache()) {
      grants.put(
          RuntimeCapabilityCatalog.STORAGE_CACHE,
          grantedStorageCapability(RuntimeCapabilityCatalog.STORAGE_CACHE));
    }
    if (storage.generated()) {
      grants.put(
          RuntimeCapabilityCatalog.STORAGE_GENERATED,
          grantedStorageCapability(RuntimeCapabilityCatalog.STORAGE_GENERATED));
    }
  }

  private void addRequestedStorageCapability(
      Map<String, GrantBuilder> grants, String capability, ModMetadata.Storage storage) {
    if (isStorageEnabled(capability, storage)) {
      grants.computeIfAbsent(capability, this::grantedStorageCapability).addSource("metadata.permissions");
      return;
    }
    grants.put(
        capability,
        new GrantBuilder(
            capability,
            RuntimeCapabilityState.DENIED,
            List.of("metadata.permissions"),
            "Runtime-2 recognizes "
                + capability
                + " but "
                + RuntimeCapabilityCatalog.storageFlag(capability)
                + " is not enabled in loader.mod.json.",
            "No Spindle ModContext "
                + RuntimeCapabilityCatalog.storageMethodName(capability)
                + " access is granted.",
            "Enable " + RuntimeCapabilityCatalog.storageFlag(capability) + " in loader.mod.json."));
  }

  private boolean isStorageEnabled(String capability, ModMetadata.Storage storage) {
    return switch (capability) {
      case RuntimeCapabilityCatalog.STORAGE_CONFIG -> storage.config();
      case RuntimeCapabilityCatalog.STORAGE_DATA -> storage.data();
      case RuntimeCapabilityCatalog.STORAGE_CACHE -> storage.cache();
      case RuntimeCapabilityCatalog.STORAGE_GENERATED -> storage.generated();
      default -> false;
    };
  }

  private GrantBuilder grantedStorageCapability(String capability) {
    return new GrantBuilder(
        capability,
        RuntimeCapabilityState.GRANTED,
        List.of(RuntimeCapabilityCatalog.storageSource(capability)),
        capability + " is enabled in loader.mod.json.",
        RuntimeCapabilityCatalog.storageControls(capability),
        null);
  }

  private static final class GrantBuilder {
    private final String capability;
    private final RuntimeCapabilityState state;
    private final LinkedHashSet<String> sources;
    private final String reason;
    private final String controls;
    private final String fix;

    private GrantBuilder(
        String capability,
        RuntimeCapabilityState state,
        List<String> sources,
        String reason,
        String controls,
        String fix) {
      this.capability = capability;
      this.state = state;
      this.sources = new LinkedHashSet<>(sources);
      this.reason = reason;
      this.controls = controls;
      this.fix = fix;
    }

    private void addSource(String source) {
      sources.add(source);
    }

    private RuntimeCapabilityGrant build() {
      return new RuntimeCapabilityGrant(
          capability, state.id(), List.copyOf(sources), reason, controls, fix);
    }
  }
}
