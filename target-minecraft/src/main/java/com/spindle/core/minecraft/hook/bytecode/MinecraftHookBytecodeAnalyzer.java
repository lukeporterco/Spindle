package com.spindle.core.minecraft.hook.bytecode;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimeClasspath;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlan;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlanner;
import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import com.spindle.core.minecraft.hook.place.MinecraftPlannedHookPlacement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

public final class MinecraftHookBytecodeAnalyzer {
  public static final String MILESTONE_NAME = "Target-6";
  private static final String TARGET = "minecraft";
  private static final String CLASS_ENTRY_NAME =
      MinecraftHookPlacementPlanner.SUPPORTED_OWNER_INTERNAL_NAME + ".class";

  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();
  private final MinecraftInstructionStreamDecoder instructionStreamDecoder =
      new MinecraftInstructionStreamDecoder();

  public MinecraftHookBytecodeAnalysisReport analyze(
      MinecraftHookPlacementPlan placementPlan,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan)
      throws LoaderException {
    if (placementPlan == null) {
      return failedReport(
          null, executionPlan, runtimePlan, null, "Target-5 hook placement plan is missing.");
    }
    if (!placementPlan.gatePassed()) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          firstPlacement(placementPlan),
          "Target-5 hook placement gate failed.");
    }
    if (!placementPlan.placementPlanned()) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          null,
          "Target-5 hook placement plan did not produce a planned placement.");
    }
    if (placementPlan.plannedPlacementCount() != 1
        || placementPlan.plannedPlacements().size() != 1) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          firstPlacement(placementPlan),
          "Target-6 requires exactly one planned hook placement.");
    }
    MinecraftPlannedHookPlacement placement = placementPlan.plannedPlacements().getFirst();
    if (!MinecraftHookPlacementPlanner.SUPPORTED_PLACEMENT_ID.equals(placement.id())) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          placement,
          "Unsupported hook placement id: " + placement.id());
    }
    if (!matchesSupportedPlacement(placement)) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          placement,
          "Target-6 requires net/minecraft/server/Main.main([Ljava/lang/String;)V.");
    }
    if (executionPlan == null
        || !MinecraftHookPlacementPlanner.SUPPORTED_MAIN_CLASS.equals(
            executionPlan.minecraftMainClass())) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          placement,
          "Minecraft execution plan main class must be net.minecraft.server.Main.");
    }
    if (runtimePlan == null) {
      return failedReport(
          placementPlan, executionPlan, null, placement, "Minecraft runtime plan is missing.");
    }

    ArtifactClassBytes classBytes = locateClassBytes(runtimePlan);
    if (classBytes.failureReason() != null) {
      return failedReport(
          placementPlan, executionPlan, runtimePlan, placement, classBytes.failureReason());
    }
    if (classBytes.classBytes() == null) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          placement,
          "Minecraft runtime artifact does not contain net/minecraft/server/Main.class.");
    }

    MinecraftDecodedCodeAttribute codeAttribute =
        methodCodeReader.readDecodedCode(
            classBytes.classBytes(),
            MinecraftHookPlacementPlanner.SUPPORTED_OWNER_INTERNAL_NAME,
            MinecraftHookPlacementPlanner.SUPPORTED_MEMBER_NAME,
            MinecraftHookPlacementPlanner.SUPPORTED_DESCRIPTOR);
    if (!codeAttribute.hasCodeAttribute()) {
      return failedReport(
          placementPlan,
          executionPlan,
          runtimePlan,
          placement,
          codeAttribute.abstractOrNative()
              ? "Minecraft server entrypoint method is abstract or native and has no Code attribute."
              : "Minecraft server entrypoint method has no Code attribute.");
    }

    MinecraftInstructionStreamDecoder.Result decodeResult =
        instructionStreamDecoder.decode(codeAttribute.code());
    boolean exceptionTableValidationPassed =
        validateExceptionHandlers(
            codeAttribute.exceptionHandlers(),
            decodeResult.instructions(),
            codeAttribute.codeLength());
    boolean gatePassed =
        decodeResult.instructionBoundaryValidationPassed()
            && decodeResult.branchTargetValidationPassed()
            && decodeResult.switchTargetValidationPassed()
            && exceptionTableValidationPassed;
    String gateFailureReason =
        gatePassed ? null : firstFailureReason(decodeResult, exceptionTableValidationPassed);

    return buildReport(
        placementPlan,
        executionPlan,
        placement,
        codeAttribute,
        decodeResult,
        exceptionTableValidationPassed,
        gatePassed,
        gateFailureReason);
  }

  private MinecraftHookBytecodeAnalysisReport buildReport(
      MinecraftHookPlacementPlan placementPlan,
      MinecraftModExecutionPlan executionPlan,
      MinecraftPlannedHookPlacement placement,
      MinecraftDecodedCodeAttribute codeAttribute,
      MinecraftInstructionStreamDecoder.Result decodeResult,
      boolean exceptionTableValidationPassed,
      boolean gatePassed,
      String gateFailureReason) {
    List<MinecraftDecodedInstruction> instructions = decodeResult.instructions();
    Integer firstInstructionOffset =
        instructions.isEmpty() ? null : instructions.getFirst().offset();
    Integer lastInstructionOffset = instructions.isEmpty() ? null : instructions.getLast().offset();
    return new MinecraftHookBytecodeAnalysisReport(
        1,
        MILESTONE_NAME,
        TARGET,
        executionPlan == null ? null : executionPlan.resolvedMinecraftVersion(),
        executionPlan == null ? null : executionPlan.side(),
        placementPlan.catalogId(),
        placementPlan.sourceContractValidationPassed(),
        placementPlan.sourceContractErrorCount(),
        executionPlan == null ? null : executionPlan.minecraftMainClass(),
        placement == null ? null : placement.id(),
        placement == null ? null : placement.sourceContractId(),
        placement == null ? null : placement.ownerInternalName(),
        placement == null ? null : placement.memberName(),
        placement == null ? null : placement.descriptor(),
        placement == null ? null : placement.bytecodeOffset(),
        gatePassed,
        gateFailureReason,
        gatePassed,
        codeAttribute.hasCodeAttribute(),
        true,
        true,
        decodeResult.instructionBoundaryValidationPassed(),
        decodeResult.branchTargetValidationPassed(),
        decodeResult.switchTargetValidationPassed(),
        exceptionTableValidationPassed,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        instructions.size(),
        firstInstructionOffset,
        lastInstructionOffset,
        codeAttribute.codeLength(),
        codeAttribute.codeSha256(),
        codeAttribute.stackMapTablePresent(),
        codeAttribute.stackMapTableEntryCount(),
        codeAttribute.nestedCodeAttributes().size(),
        codeAttribute.exceptionHandlers().size(),
        decodeResult.returnInstructionCount(),
        decodeResult.throwInstructionCount(),
        decodeResult.invokeInstructionCount(),
        decodeResult.branchInstructionCount(),
        decodeResult.switchInstructionCount(),
        decodeResult.wideInstructionCount(),
        decodeResult.reservedOpcodeCount(),
        decodeResult.unsupportedOpcodeCount(),
        decodeResult.methodEntryInstructionBoundary(),
        instructions,
        codeAttribute.exceptionHandlers(),
        codeAttribute.nestedCodeAttributes());
  }

  private MinecraftHookBytecodeAnalysisReport failedReport(
      MinecraftHookPlacementPlan placementPlan,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan,
      MinecraftPlannedHookPlacement placement,
      String reason) {
    return new MinecraftHookBytecodeAnalysisReport(
        1,
        MILESTONE_NAME,
        TARGET,
        executionPlan != null
            ? executionPlan.resolvedMinecraftVersion()
            : runtimePlan == null ? null : runtimePlan.resolvedMinecraftVersion(),
        executionPlan != null ? executionPlan.side() : null,
        placementPlan == null ? null : placementPlan.catalogId(),
        placementPlan != null && placementPlan.sourceContractValidationPassed(),
        placementPlan == null ? 0 : placementPlan.sourceContractErrorCount(),
        executionPlan == null ? null : executionPlan.minecraftMainClass(),
        placement == null ? null : placement.id(),
        placement == null ? null : placement.sourceContractId(),
        placement == null ? null : placement.ownerInternalName(),
        placement == null ? null : placement.memberName(),
        placement == null ? null : placement.descriptor(),
        placement == null ? null : placement.bytecodeOffset(),
        false,
        reason,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        0,
        null,
        null,
        null,
        null,
        false,
        null,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        false,
        List.of(),
        List.of(),
        List.of());
  }

  private static String firstFailureReason(
      MinecraftInstructionStreamDecoder.Result decodeResult,
      boolean exceptionTableValidationPassed) {
    if (!decodeResult.instructionBoundaryValidationPassed()
        || !decodeResult.branchTargetValidationPassed()
        || !decodeResult.switchTargetValidationPassed()) {
      return decodeResult.validationFailureReason();
    }
    if (!exceptionTableValidationPassed) {
      return "Exception table targets do not align with decoded instruction boundaries.";
    }
    return "Bytecode analysis validation failed.";
  }

  private boolean validateExceptionHandlers(
      List<MinecraftDecodedExceptionHandler> exceptionHandlers,
      List<MinecraftDecodedInstruction> instructions,
      Integer codeLength) {
    if (codeLength == null) {
      return false;
    }
    Set<Integer> instructionOffsets = new TreeSet<>();
    for (MinecraftDecodedInstruction instruction : instructions) {
      instructionOffsets.add(instruction.offset());
    }
    for (MinecraftDecodedExceptionHandler handler : exceptionHandlers) {
      if (!instructionOffsets.contains(handler.startPc())) {
        return false;
      }
      if (!instructionOffsets.contains(handler.handlerPc())) {
        return false;
      }
      if (handler.endPc() != codeLength && !instructionOffsets.contains(handler.endPc())) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesSupportedPlacement(MinecraftPlannedHookPlacement placement) {
    return MinecraftHookPlacementPlanner.SUPPORTED_OWNER_INTERNAL_NAME.equals(
            placement.ownerInternalName())
        && MinecraftHookPlacementPlanner.SUPPORTED_MEMBER_NAME.equals(placement.memberName())
        && MinecraftHookPlacementPlanner.SUPPORTED_DESCRIPTOR.equals(placement.descriptor());
  }

  private ArtifactClassBytes locateClassBytes(MinecraftServerRuntimePlan runtimePlan) {
    for (Path artifactPath : artifactCandidates(runtimePlan)) {
      if (!Files.isRegularFile(artifactPath)) {
        continue;
      }
      try (JarFile jarFile = new JarFile(artifactPath.toFile())) {
        var entry = jarFile.getJarEntry(CLASS_ENTRY_NAME);
        if (entry == null) {
          continue;
        }
        try (var inputStream = jarFile.getInputStream(entry)) {
          return new ArtifactClassBytes(inputStream.readAllBytes(), null);
        }
      } catch (IOException exception) {
        return new ArtifactClassBytes(
            null,
            "Failed to inspect runtime artifact " + artifactPath.toString().replace('\\', '/'));
      }
    }
    return new ArtifactClassBytes(null, null);
  }

  private List<Path> artifactCandidates(MinecraftServerRuntimePlan runtimePlan) {
    Set<Path> candidates = new LinkedHashSet<>();
    addCandidate(candidates, runtimePlan.serverJarPath());
    for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
      addCandidate(candidates, entry.path());
    }
    for (MinecraftRuntimeFile file : runtimePlan.bundledRuntimeFiles()) {
      if (file.path() != null) {
        candidates.add(file.path().toAbsolutePath().normalize());
      }
    }
    return List.copyOf(candidates);
  }

  private void addCandidate(Set<Path> candidates, String pathValue) {
    if (pathValue == null || pathValue.isBlank()) {
      return;
    }
    candidates.add(Path.of(pathValue).toAbsolutePath().normalize());
  }

  private static MinecraftPlannedHookPlacement firstPlacement(
      MinecraftHookPlacementPlan placementPlan) {
    return placementPlan == null || placementPlan.plannedPlacements().isEmpty()
        ? null
        : placementPlan.plannedPlacements().getFirst();
  }

  private record ArtifactClassBytes(byte[] classBytes, String failureReason) {}
}
