package com.spindle.core.security.trust;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.security.SecurityFinding;
import com.spindle.core.security.SecurityLocation;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.core.security.SecuritySeverity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArtifactTrustEvaluator {
  private static final Comparator<ArtifactTrustEntry> ENTRY_ORDER =
      Comparator.comparing(ArtifactTrustEntry::path)
          .thenComparing(ArtifactTrustEntry::modId)
          .thenComparing(ArtifactTrustEntry::version)
          .thenComparing(ArtifactTrustEntry::sha256);

  private final ArtifactSignatureReader signatureReader = new ArtifactSignatureReader();
  private final ArtifactSignatureVerifier signatureVerifier = new ArtifactSignatureVerifier();

  public ArtifactTrustEvaluation evaluate(
      List<ResolvedModSet.ResolvedMod> mods, String lockfileAction) throws LoaderException {
    List<ArtifactTrustEntry> entries = new ArrayList<>(mods.size());
    List<SecurityFinding> findings = new ArrayList<>();
    for (ResolvedModSet.ResolvedMod mod :
        mods.stream()
            .sorted(Comparator.comparing(ResolvedModSet.ResolvedMod::normalizedRelativePath))
            .toList()) {
      evaluateMod(mod, lockfileAction, entries, findings);
    }
    List<ArtifactTrustEntry> sortedEntries = entries.stream().sorted(ENTRY_ORDER).toList();
    return new ArtifactTrustEvaluation(
        sortedEntries, ArtifactTrustSummary.from(sortedEntries), findings);
  }

  private void evaluateMod(
      ResolvedModSet.ResolvedMod mod,
      String lockfileAction,
      List<ArtifactTrustEntry> entries,
      List<SecurityFinding> findings)
      throws LoaderException {
    Path sidecarPath = sidecarPath(mod.jarPath());
    String sidecarDisplayPath =
        normalizeRelativePath(mod.relativePath()) + ".spindle-signature.json";
    if (!Files.isRegularFile(sidecarPath)) {
      ArtifactTrustState trustState =
          "verified".equals(lockfileAction)
              ? ArtifactTrustState.LOCKED_HASH
              : ArtifactTrustState.LOCAL_UNSIGNED;
      ArtifactTrustEntry entry =
          new ArtifactTrustEntry(
              mod.id(),
              mod.version(),
              normalizeRelativePath(mod.relativePath()),
              mod.sha256(),
              trustState,
              null,
              null);
      entries.add(entry);
      addUnsignedFindings(entry, findings);
      return;
    }

    ArtifactSignatureSidecar sidecar;
    try {
      sidecar = signatureReader.read(sidecarPath);
    } catch (ArtifactSignatureReader.SidecarReadException exception) {
      ArtifactTrustEntry entry =
          new ArtifactTrustEntry(
              mod.id(),
              mod.version(),
              normalizeRelativePath(mod.relativePath()),
              mod.sha256(),
              ArtifactTrustState.SIGNATURE_SIDECAR_INVALID,
              null,
              null);
      entries.add(entry);
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_002,
              SecuritySeverity.FATAL,
              mod.id(),
              SecurityLocation.of("signature", sidecarDisplayPath),
              "Artifact signature sidecar `" + sidecarDisplayPath + "` " + exception.getMessage(),
              "Rewrite the sidecar in schemaVersion `1` with supported `spindle-ed25519` fields, or remove the sidecar until a valid signature is available."));
      return;
    }

    ArtifactTrustEntry provisionalEntry =
        new ArtifactTrustEntry(
            mod.id(),
            mod.version(),
            normalizeRelativePath(mod.relativePath()),
            mod.sha256(),
            ArtifactTrustState.SIGNED_ARTIFACT,
            sidecar.signerId(),
            sidecar.signatureKind());
    try {
      signatureVerifier.verify(sidecar, mod);
      entries.add(provisionalEntry);
    } catch (ArtifactSignatureVerifier.VerificationException exception) {
      ArtifactTrustState trustState =
          switch (exception.kind()) {
            case SIDECAR_INVALID -> ArtifactTrustState.SIGNATURE_SIDECAR_INVALID;
            case ARTIFACT_HASH_MISMATCH -> ArtifactTrustState.SIGNATURE_ARTIFACT_HASH_MISMATCH;
            case SIGNATURE_INVALID -> ArtifactTrustState.SIGNATURE_INVALID;
          };
      ArtifactTrustEntry invalidEntry =
          new ArtifactTrustEntry(
              mod.id(),
              mod.version(),
              normalizeRelativePath(mod.relativePath()),
              mod.sha256(),
              trustState,
              sidecar.signerId(),
              sidecar.signatureKind());
      entries.add(invalidEntry);
      findings.add(findingForVerificationFailure(mod, sidecarDisplayPath, exception));
    }
  }

  private void addUnsignedFindings(ArtifactTrustEntry entry, List<SecurityFinding> findings) {
    if (entry.trustState() == ArtifactTrustState.LOCAL_UNSIGNED) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_001,
              SecuritySeverity.WARNING,
              entry.modId(),
              SecurityLocation.of("artifact", entry.path()),
              "Artifact `"
                  + entry.path()
                  + "` is a local unsigned jar. Spindle will run it for local development, but no publisher identity was verified.",
              "Use a detached `.spindle-signature.json` sidecar for publisher identity, or keep this warning during local development."));
    } else if (entry.trustState() == ArtifactTrustState.LOCKED_HASH) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_005,
              SecuritySeverity.WARNING,
              entry.modId(),
              SecurityLocation.of("artifact", entry.path()),
              "Artifact `"
                  + entry.path()
                  + "` matches the current `spindle.lock.json` hash identity, but no publisher identity is present.",
              "Keep relying on lockfile hashes for repeatability, or add a detached signature sidecar when publisher identity matters."));
    }
    if (entry.provenanceState() == ArtifactProvenanceState.NOT_PRESENT) {
      findings.add(
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_006,
              SecuritySeverity.WARNING,
              entry.modId(),
              SecurityLocation.of("artifact", entry.path()),
              "Artifact `"
                  + entry.path()
                  + "` does not include provenance beyond local file placement and optional lockfile hash identity.",
              "Treat the jar as locally sourced unless you can supply a verified publisher signature or an audited distribution workflow."));
    }
  }

  private SecurityFinding findingForVerificationFailure(
      ResolvedModSet.ResolvedMod mod,
      String sidecarDisplayPath,
      ArtifactSignatureVerifier.VerificationException exception) {
    return switch (exception.kind()) {
      case SIDECAR_INVALID ->
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_002,
              SecuritySeverity.FATAL,
              mod.id(),
              SecurityLocation.of("signature", sidecarDisplayPath),
              "Artifact signature sidecar `"
                  + sidecarDisplayPath
                  + "` is malformed or unsupported: "
                  + exception.getMessage(),
              "Rewrite the sidecar with a valid Ed25519 public key and supported Spindle signature fields, or remove the sidecar until it is valid.");
      case ARTIFACT_HASH_MISMATCH ->
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_003,
              SecuritySeverity.FATAL,
              mod.id(),
              SecurityLocation.of("artifact", normalizeRelativePath(mod.relativePath())),
              "Signed artifact `"
                  + normalizeRelativePath(mod.relativePath())
                  + "` does not match the hash claimed by `"
                  + sidecarDisplayPath
                  + "`: "
                  + exception.getMessage(),
              "Restore the expected jar bytes or regenerate the sidecar against the exact artifact that will be distributed.");
      case SIGNATURE_INVALID ->
          new SecurityFinding(
              SecurityRuleId.SEC_TRUST_004,
              SecuritySeverity.FATAL,
              mod.id(),
              SecurityLocation.of("signature", sidecarDisplayPath),
              "Artifact signature verification failed for `"
                  + sidecarDisplayPath
                  + "`: "
                  + exception.getMessage(),
              "Re-sign the exact jar with the matching signer key and signed fields, or remove the sidecar until the signature is correct.");
    };
  }

  private Path sidecarPath(Path jarPath) {
    return jarPath.resolveSibling(jarPath.getFileName().toString() + ".spindle-signature.json");
  }

  private String normalizeRelativePath(Path path) {
    return path.toString().replace('\\', '/');
  }
}
