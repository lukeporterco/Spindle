package com.spindle.core.minecraft.hook;

import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MinecraftHookContractValidator {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-2";
  private static final String TARGET = "minecraft";
  private static final String EMPTY_CATALOG_CODE = "target-2.no_contracts_declared";
  private static final String EMPTY_CATALOG_MESSAGE =
      "Target-2 defines hook contract diagnostics, but no hook contracts are declared yet.";

  public MinecraftHookContractReport validate(
      MinecraftArtifactInterpretation interpretation, MinecraftHookContractCatalog catalog) {
    Objects.requireNonNull(interpretation, "interpretation");
    Objects.requireNonNull(catalog, "catalog");

    MinecraftHookContractIndexes indexes = MinecraftHookContractIndexes.from(interpretation);
    List<PendingResult> pendingResults = new ArrayList<>();
    List<DiagnosticCandidate> diagnosticCandidates = new ArrayList<>();

    if (catalog.contracts().isEmpty()) {
      diagnosticCandidates.add(
          new DiagnosticCandidate(
              Integer.MAX_VALUE,
              -1,
              null,
              MinecraftHookDiagnosticSeverity.INFO,
              MinecraftHookContractStatus.NO_CONTRACTS_DECLARED,
              EMPTY_CATALOG_CODE,
              EMPTY_CATALOG_MESSAGE,
              null,
              null,
              null));
      return buildReport(interpretation, pendingResults, diagnosticCandidates);
    }

    Set<String> seenIds = new HashSet<>();
    for (int contractIndex = 0; contractIndex < catalog.contracts().size(); contractIndex++) {
      MinecraftHookPointContract contract = catalog.contracts().get(contractIndex);
      ValidationResult validation = validateContract(contract, interpretation, indexes, seenIds);
      pendingResults.add(
          new PendingResult(
              contract.id(),
              contract.description(),
              contract.side() == null ? null : contract.side().id(),
              contract.kind() == null ? null : contract.kind().name(),
              contract.ownerInternalName(),
              contract.memberName(),
              contract.descriptor(),
              contract.requirement() == null ? null : contract.requirement().name(),
              validation.status().name(),
              validation.valid(),
              contract.requirement() == MinecraftHookRequirement.REQUIRED,
              contract.requirement() == MinecraftHookRequirement.OPTIONAL,
              validation.matchedClass(),
              validation.matchedMember()));
      if (validation.diagnosticSeverity() != null) {
        diagnosticCandidates.add(
            new DiagnosticCandidate(
                contractIndex,
                pendingResults.size() - 1,
                contract.id(),
                validation.diagnosticSeverity(),
                validation.status(),
                validation.code(),
                validation.message(),
                contract.ownerInternalName(),
                contract.memberName(),
                contract.descriptor()));
      }
    }

    return buildReport(interpretation, pendingResults, diagnosticCandidates);
  }

  private ValidationResult validateContract(
      MinecraftHookPointContract contract,
      MinecraftArtifactInterpretation interpretation,
      MinecraftHookContractIndexes indexes,
      Set<String> seenIds) {
    Objects.requireNonNull(contract, "contract");

    String malformedMessage = malformedMessage(contract);
    if (malformedMessage != null) {
      return invalid(
          MinecraftHookContractStatus.MALFORMED_CONTRACT,
          severityFor(contract.requirement()),
          "target-2.malformed_contract",
          malformedMessage);
    }

    if (!seenIds.add(contract.id())) {
      return invalid(
          MinecraftHookContractStatus.DUPLICATE_ID,
          MinecraftHookDiagnosticSeverity.ERROR,
          "target-2.duplicate_id",
          "Hook contract `" + contract.id() + "` is declared more than once.");
    }

    if (!contract.side().id().equals(interpretation.side())) {
      return invalid(
          MinecraftHookContractStatus.SIDE_MISMATCH,
          severityFor(contract.requirement()),
          "target-2.side_mismatch",
          "Hook contract `"
              + contract.id()
              + "` targets side `"
              + contract.side().id()
              + "`, but the interpreted artifact is `"
              + interpretation.side()
              + "`.");
    }

    if (!indexes.hasClass(contract.ownerInternalName())) {
      return invalid(
          MinecraftHookContractStatus.MISSING_CLASS,
          severityFor(contract.requirement()),
          "target-2.missing_class",
          "Hook contract `"
              + contract.id()
              + "` expected class `"
              + contract.ownerInternalName()
              + "` to exist in the interpreted Minecraft artifact.");
    }

    return switch (contract.kind()) {
      case CLASS -> valid(contract.ownerInternalName(), null);
      case METHOD ->
          indexes.hasMethod(
                  contract.ownerInternalName(), contract.memberName(), contract.descriptor())
              ? valid(contract.ownerInternalName(), contract.memberName() + contract.descriptor())
              : invalid(
                  MinecraftHookContractStatus.MISSING_MEMBER,
                  severityFor(contract.requirement()),
                  "target-2.missing_member",
                  "Hook contract `"
                      + contract.id()
                      + "` expected method `"
                      + contract.memberName()
                      + contract.descriptor()
                      + "` on class `"
                      + contract.ownerInternalName()
                      + "`.");
      case CONSTRUCTOR ->
          indexes.hasConstructor(contract.ownerInternalName(), contract.descriptor())
              ? valid(contract.ownerInternalName(), contract.memberName() + contract.descriptor())
              : invalid(
                  MinecraftHookContractStatus.MISSING_MEMBER,
                  severityFor(contract.requirement()),
                  "target-2.missing_member",
                  "Hook contract `"
                      + contract.id()
                      + "` expected constructor `"
                      + contract.memberName()
                      + contract.descriptor()
                      + "` on class `"
                      + contract.ownerInternalName()
                      + "`.");
      case FIELD ->
          indexes.hasField(
                  contract.ownerInternalName(), contract.memberName(), contract.descriptor())
              ? valid(
                  contract.ownerInternalName(), contract.memberName() + ":" + contract.descriptor())
              : invalid(
                  MinecraftHookContractStatus.MISSING_MEMBER,
                  severityFor(contract.requirement()),
                  "target-2.missing_member",
                  "Hook contract `"
                      + contract.id()
                      + "` expected field `"
                      + contract.memberName()
                      + ":"
                      + contract.descriptor()
                      + "` on class `"
                      + contract.ownerInternalName()
                      + "`.");
    };
  }

  private MinecraftHookContractReport buildReport(
      MinecraftArtifactInterpretation interpretation,
      List<PendingResult> pendingResults,
      List<DiagnosticCandidate> diagnosticCandidates) {
    List<List<String>> resultDiagnosticIds = new ArrayList<>();
    for (int index = 0; index < pendingResults.size(); index++) {
      resultDiagnosticIds.add(new ArrayList<>());
    }

    List<DiagnosticCandidate> sortedCandidates =
        diagnosticCandidates.stream()
            .sorted(
                Comparator.comparingInt(DiagnosticCandidate::contractIndex)
                    .thenComparing(DiagnosticCandidate::code)
                    .thenComparing(
                        candidate -> candidate.contractId() == null ? "" : candidate.contractId())
                    .thenComparing(
                        candidate ->
                            candidate.ownerInternalName() == null
                                ? ""
                                : candidate.ownerInternalName())
                    .thenComparing(
                        candidate -> candidate.memberName() == null ? "" : candidate.memberName())
                    .thenComparing(
                        candidate -> candidate.descriptor() == null ? "" : candidate.descriptor()))
            .toList();

    List<MinecraftHookContractDiagnostic> diagnostics = new ArrayList<>();
    for (int index = 0; index < sortedCandidates.size(); index++) {
      DiagnosticCandidate candidate = sortedCandidates.get(index);
      String diagnosticId = "hook-contract-%04d".formatted(index + 1);
      diagnostics.add(
          new MinecraftHookContractDiagnostic(
              diagnosticId,
              candidate.severity(),
              candidate.status().name(),
              candidate.contractId(),
              candidate.code(),
              candidate.message(),
              candidate.ownerInternalName(),
              candidate.memberName(),
              candidate.descriptor()));
      if (candidate.resultIndex() >= 0) {
        resultDiagnosticIds.get(candidate.resultIndex()).add(diagnosticId);
      }
    }

    List<MinecraftHookContractResult> results = new ArrayList<>();
    for (int index = 0; index < pendingResults.size(); index++) {
      PendingResult pendingResult = pendingResults.get(index);
      results.add(
          new MinecraftHookContractResult(
              pendingResult.id(),
              pendingResult.description(),
              pendingResult.side(),
              pendingResult.kind(),
              pendingResult.ownerInternalName(),
              pendingResult.memberName(),
              pendingResult.descriptor(),
              pendingResult.requirement(),
              pendingResult.status(),
              pendingResult.valid(),
              pendingResult.required(),
              pendingResult.optional(),
              resultDiagnosticIds.get(index),
              pendingResult.matchedClass(),
              pendingResult.matchedMember()));
    }

    int validContractCount =
        (int) results.stream().filter(MinecraftHookContractResult::valid).count();
    int invalidContractCount = results.size() - validContractCount;
    int requiredContractCount =
        (int) results.stream().filter(MinecraftHookContractResult::required).count();
    int optionalContractCount =
        (int) results.stream().filter(MinecraftHookContractResult::optional).count();
    int warningCount =
        (int)
            diagnostics.stream()
                .filter(
                    diagnostic -> diagnostic.severity() == MinecraftHookDiagnosticSeverity.WARNING)
                .count();
    int errorCount =
        (int)
            diagnostics.stream()
                .filter(
                    diagnostic -> diagnostic.severity() == MinecraftHookDiagnosticSeverity.ERROR)
                .count();

    return new MinecraftHookContractReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        interpretation.minecraftVersion(),
        interpretation.side(),
        true,
        false,
        false,
        false,
        false,
        false,
        interpretation.schema(),
        interpretation.milestoneName(),
        results.size(),
        validContractCount,
        invalidContractCount,
        requiredContractCount,
        optionalContractCount,
        warningCount,
        errorCount,
        errorCount == 0,
        results,
        diagnostics);
  }

  private String malformedMessage(MinecraftHookPointContract contract) {
    if (isBlank(contract.id())) {
      return "Hook contract ids must be nonblank.";
    }
    if (contract.side() == null) {
      return "Hook contract `" + contract.id() + "` must declare a side.";
    }
    if (contract.kind() == null) {
      return "Hook contract `" + contract.id() + "` must declare a hook point kind.";
    }
    if (contract.requirement() == null) {
      return "Hook contract `" + contract.id() + "` must declare a requirement level.";
    }
    if (!isInternalName(contract.ownerInternalName())) {
      return "Hook contract `"
          + contract.id()
          + "` must use a slash-style owner internal name such as `net/minecraft/server/MinecraftServer`.";
    }
    return switch (contract.kind()) {
      case CLASS -> null;
      case METHOD ->
          isBlank(contract.memberName()) || isBlank(contract.descriptor())
              ? "Hook method contract `"
                  + contract.id()
                  + "` must declare both memberName and descriptor."
              : null;
      case CONSTRUCTOR ->
          !"<init>".equals(contract.memberName()) || isBlank(contract.descriptor())
              ? "Hook constructor contract `"
                  + contract.id()
                  + "` must use memberName `<init>` and a nonblank descriptor."
              : null;
      case FIELD ->
          isBlank(contract.memberName()) || isBlank(contract.descriptor())
              ? "Hook field contract `"
                  + contract.id()
                  + "` must declare both memberName and descriptor."
              : null;
    };
  }

  private boolean isInternalName(String value) {
    return !isBlank(value) && !value.contains(".") && !value.contains("\\");
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static MinecraftHookDiagnosticSeverity severityFor(MinecraftHookRequirement requirement) {
    return requirement == MinecraftHookRequirement.OPTIONAL
        ? MinecraftHookDiagnosticSeverity.WARNING
        : MinecraftHookDiagnosticSeverity.ERROR;
  }

  private static ValidationResult valid(String matchedClass, String matchedMember) {
    return new ValidationResult(
        MinecraftHookContractStatus.VALID, true, null, null, null, matchedClass, matchedMember);
  }

  private static ValidationResult invalid(
      MinecraftHookContractStatus status,
      MinecraftHookDiagnosticSeverity diagnosticSeverity,
      String code,
      String message) {
    return new ValidationResult(status, false, diagnosticSeverity, code, message, null, null);
  }

  private record ValidationResult(
      MinecraftHookContractStatus status,
      boolean valid,
      MinecraftHookDiagnosticSeverity diagnosticSeverity,
      String code,
      String message,
      String matchedClass,
      String matchedMember) {}

  private record PendingResult(
      String id,
      String description,
      String side,
      String kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      String requirement,
      String status,
      boolean valid,
      boolean required,
      boolean optional,
      String matchedClass,
      String matchedMember) {}

  private record DiagnosticCandidate(
      int contractIndex,
      int resultIndex,
      String contractId,
      MinecraftHookDiagnosticSeverity severity,
      MinecraftHookContractStatus status,
      String code,
      String message,
      String ownerInternalName,
      String memberName,
      String descriptor) {}
}
