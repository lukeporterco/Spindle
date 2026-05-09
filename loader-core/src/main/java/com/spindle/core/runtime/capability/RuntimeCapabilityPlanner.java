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
    addGrantedServiceCapabilities(grants, mod);
    for (String requestedCapability : mod.permissions()) {
      if (RuntimeCapabilityCatalog.isGrantableStorage(requestedCapability)) {
        addRequestedStorageCapability(grants, requestedCapability, mod.storage());
        continue;
      }
      if (RuntimeCapabilityCatalog.SERVICE_PROVIDE.equals(requestedCapability)
          || RuntimeCapabilityCatalog.SERVICE_CONSUME.equals(requestedCapability)) {
        addRequestedServiceCapability(grants, requestedCapability, mod);
        continue;
      }
      if (RuntimeCapabilityCatalog.isUnavailable(requestedCapability)) {
        grants.put(
            requestedCapability,
            new GrantBuilder(
                requestedCapability,
                RuntimeCapabilityState.UNAVAILABLE,
                List.of("metadata.permissions"),
                "Runtime-3 recognizes "
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
                "Runtime-3 records "
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
              "Runtime-3 does not recognize requested capability `" + requestedCapability + "`.",
              "No Spindle API surface is granted for unknown capabilities.",
              "Rename the capability to a Runtime-3 catalog entry or keep it as documentation only."));
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
            "Runtime-3 recognizes "
                + capability
                + " but "
                + RuntimeCapabilityCatalog.storageFlag(capability)
                + " is not enabled in loader.mod.json.",
            "No Spindle ModContext "
                + RuntimeCapabilityCatalog.storageMethodName(capability)
                + " access is granted.",
            "Enable " + RuntimeCapabilityCatalog.storageFlag(capability) + " in loader.mod.json."));
  }

  private void addGrantedServiceCapabilities(
      Map<String, GrantBuilder> grants, ResolvedModSet.ResolvedMod mod) {
    if (!mod.services().provides().isEmpty()) {
      grants.put(
          RuntimeCapabilityCatalog.SERVICE_PROVIDE,
          grantedServiceCapability(RuntimeCapabilityCatalog.SERVICE_PROVIDE));
    }
    if (!mod.services().consumes().isEmpty()) {
      grants.put(
          RuntimeCapabilityCatalog.SERVICE_CONSUME,
          grantedServiceCapability(RuntimeCapabilityCatalog.SERVICE_CONSUME));
    }
  }

  private void addRequestedServiceCapability(
      Map<String, GrantBuilder> grants, String capability, ResolvedModSet.ResolvedMod mod) {
    boolean declared =
        switch (capability) {
          case RuntimeCapabilityCatalog.SERVICE_PROVIDE -> !mod.services().provides().isEmpty();
          case RuntimeCapabilityCatalog.SERVICE_CONSUME -> !mod.services().consumes().isEmpty();
          default -> false;
        };
    if (declared) {
      grants.computeIfAbsent(capability, this::grantedServiceCapability).addSource("metadata.permissions");
      return;
    }
    grants.put(
        capability,
        new GrantBuilder(
            capability,
            RuntimeCapabilityState.DENIED,
            List.of("metadata.permissions"),
            "Runtime-3 recognizes "
                + capability
                + " but matching service declarations are missing in loader.mod.json.",
            switch (capability) {
              case RuntimeCapabilityCatalog.SERVICE_PROVIDE ->
                  "No Spindle runtime service provider contract is granted.";
              case RuntimeCapabilityCatalog.SERVICE_CONSUME ->
                  "No Spindle ModContext services() access is granted.";
              default -> throw new IllegalArgumentException("Unsupported service capability " + capability);
            },
            switch (capability) {
              case RuntimeCapabilityCatalog.SERVICE_PROVIDE ->
                  "Declare one or more services.provides entries in loader.mod.json.";
              case RuntimeCapabilityCatalog.SERVICE_CONSUME ->
                  "Declare one or more services.consumes entries in loader.mod.json.";
              default -> throw new IllegalArgumentException("Unsupported service capability " + capability);
            }));
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

  private GrantBuilder grantedServiceCapability(String capability) {
    return new GrantBuilder(
        capability,
        RuntimeCapabilityState.GRANTED,
        List.of(RuntimeCapabilityCatalog.serviceSource(capability)),
        switch (capability) {
          case RuntimeCapabilityCatalog.SERVICE_PROVIDE ->
              "service.provide is granted because services.provides declares one or more service providers.";
          case RuntimeCapabilityCatalog.SERVICE_CONSUME ->
              "service.consume is granted because services.consumes declares one or more service consumers.";
          default -> throw new IllegalArgumentException("Unsupported service capability " + capability);
        },
        switch (capability) {
          case RuntimeCapabilityCatalog.SERVICE_PROVIDE ->
              "Spindle compiles deterministic runtime service provider plans only.";
          case RuntimeCapabilityCatalog.SERVICE_CONSUME ->
              "Spindle ModContext services() access is limited to declared service bindings only.";
          default -> throw new IllegalArgumentException("Unsupported service capability " + capability);
        },
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
