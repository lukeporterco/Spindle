package com.spindle.core.minecraft.lifecycle;

import com.spindle.core.minecraft.concept.MinecraftTargetConcept;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import java.util.List;
import java.util.Objects;

public final class MinecraftServerLifecycleBindingAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-11";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.server_lifecycle";
  private static final String STARTING_BINDING_KIND = "known-main-entrypoint-analysis";
  private static final String STARTING_CONTRACT_ID = "minecraft.26_1_2.server.main.entrypoint";
  private static final String STARTING_OWNER = "net/minecraft/server/Main";
  private static final String STARTING_MEMBER = "main";
  private static final String STARTING_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final String VALIDATION_GATE_FAILURE_REASON =
      "Target-11 requires a passing Target-3 hook contract validation report.";
  private static final String ENTRYPOINT_GATE_FAILURE_REASON =
      "Target-11 requires valid hook contract `minecraft.26_1_2.server.main.entrypoint`.";

  public MinecraftServerLifecycleBindingReport analyze(
      MinecraftTargetConceptCatalog conceptCatalog,
      MinecraftHookContractReport hookContractReport) {
    Objects.requireNonNull(conceptCatalog, "conceptCatalog");
    Objects.requireNonNull(hookContractReport, "hookContractReport");

    MinecraftTargetConcept concept =
        conceptCatalog
            .findById(CONCEPT_ID)
            .orElseThrow(
                () -> new IllegalArgumentException("Missing concept `" + CONCEPT_ID + "`."));

    MinecraftHookContractResult startingContract =
        hookContractReport.contracts().stream()
            .filter(contract -> STARTING_CONTRACT_ID.equals(contract.id()))
            .findFirst()
            .orElse(null);

    boolean sourceContractValidationPassed = hookContractReport.validationPassed();
    boolean gatePassed =
        sourceContractValidationPassed && isValidEntrypointContract(startingContract);
    String gateFailureReason =
        gatePassed
            ? null
            : (sourceContractValidationPassed
                ? ENTRYPOINT_GATE_FAILURE_REASON
                : VALIDATION_GATE_FAILURE_REASON);

    List<MinecraftServerLifecycleBinding> bindings =
        List.of(
            startingBinding(gatePassed, gateFailureReason),
            declaredUnboundBinding(MinecraftServerLifecyclePhase.SERVER_STARTED),
            declaredUnboundBinding(MinecraftServerLifecyclePhase.SERVER_STOPPING),
            declaredUnboundBinding(MinecraftServerLifecyclePhase.SERVER_STOPPED),
            declaredUnboundBinding(MinecraftServerLifecyclePhase.SERVER_CRASHED),
            declaredUnboundBinding(MinecraftServerLifecyclePhase.SERVER_RELOAD_REQUESTED));

    int boundPhaseCount =
        (int)
            bindings.stream()
                .filter(binding -> binding.status() == MinecraftServerLifecycleBindingStatus.BOUND)
                .count();
    int unboundPhaseCount =
        (int)
            bindings.stream()
                .filter(
                    binding ->
                        binding.status() == MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND)
                .count();

    return new MinecraftServerLifecycleBindingReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        hookContractReport.minecraftVersion(),
        hookContractReport.side(),
        concept.id(),
        concept.order(),
        concept.displayName(),
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        hookContractReport.catalogId(),
        sourceContractValidationPassed,
        gatePassed,
        gateFailureReason,
        MinecraftServerLifecyclePhase.values().length,
        boundPhaseCount,
        unboundPhaseCount,
        bindings.size(),
        bindings);
  }

  private MinecraftServerLifecycleBinding startingBinding(
      boolean gatePassed, String gateFailureReason) {
    return new MinecraftServerLifecycleBinding(
        bindingId(MinecraftServerLifecyclePhase.SERVER_STARTING),
        MinecraftServerLifecyclePhase.SERVER_STARTING.id(),
        MinecraftServerLifecyclePhase.SERVER_STARTING.displayName(),
        gatePassed
            ? MinecraftServerLifecycleBindingStatus.BOUND
            : MinecraftServerLifecycleBindingStatus.UNSUPPORTED,
        true,
        STARTING_CONTRACT_ID,
        STARTING_OWNER,
        STARTING_MEMBER,
        STARTING_DESCRIPTOR,
        STARTING_BINDING_KIND,
        gatePassed
            ? "Bound to the known Minecraft dedicated server main entrypoint startup boundary."
            : gateFailureReason);
  }

  private MinecraftServerLifecycleBinding declaredUnboundBinding(
      MinecraftServerLifecyclePhase phase) {
    return new MinecraftServerLifecycleBinding(
        bindingId(phase),
        phase.id(),
        phase.displayName(),
        MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
        false,
        null,
        null,
        null,
        null,
        null,
        "Declared for future Target Layer lifecycle work but unbound in this pass.");
  }

  private boolean isValidEntrypointContract(MinecraftHookContractResult contract) {
    return contract != null
        && contract.valid()
        && STARTING_CONTRACT_ID.equals(contract.id())
        && STARTING_OWNER.equals(contract.ownerInternalName())
        && STARTING_MEMBER.equals(contract.memberName())
        && STARTING_DESCRIPTOR.equals(contract.descriptor());
  }

  private String bindingId(MinecraftServerLifecyclePhase phase) {
    return "target-11." + phase.id();
  }
}
