package com.spindle.core.lifecycle;

import com.spindle.api.ModInitializer;
import com.spindle.api.lifecycle.LifecyclePhase;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LifecyclePlanBuilder {
  public LifecyclePlan build(ResolvedModSet resolvedMods, ClassOwnershipIndex classOwnershipIndex) {
    List<String> phaseOrder = Arrays.stream(LifecyclePhase.values()).map(Enum::name).toList();
    List<LifecycleHandlerDeclaration> handlers = new ArrayList<>();

    for (LifecyclePhase phase : LifecyclePhase.values()) {
      for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
        if (phase == LifecyclePhase.BOOTSTRAP && mod.metadataSchema() == 1) {
          for (String entrypointClassName : mod.entrypoints().getOrDefault("main", List.of())) {
            handlers.add(
                new LifecycleHandlerDeclaration(
                    phase.name(),
                    mod.id(),
                    classOwnershipIndex.ownerOfClass(entrypointClassName).orElse(mod.id()),
                    LifecycleHandlerDeclaration.KIND_LEGACY_MOD_INITIALIZER,
                    entrypointClassName,
                    "onInitialize",
                    ModInitializer.class.getName(),
                    mod.normalizedRelativePath(),
                    mod.sha256()));
          }
        }

        for (String declaration : mod.lifecycle().getOrDefault(phase.name(), List.of())) {
          int separator = declaration.indexOf("::");
          handlers.add(
              new LifecycleHandlerDeclaration(
                  phase.name(),
                  mod.id(),
                  classOwnershipIndex
                      .ownerOfClass(declaration.substring(0, separator))
                      .orElse(mod.id()),
                  LifecycleHandlerDeclaration.KIND_STATIC_METHOD,
                  declaration.substring(0, separator),
                  declaration.substring(separator + 2),
                  null,
                  mod.normalizedRelativePath(),
                  mod.sha256()));
        }
      }
    }

    return new LifecyclePlan(phaseOrder, handlers);
  }
}
