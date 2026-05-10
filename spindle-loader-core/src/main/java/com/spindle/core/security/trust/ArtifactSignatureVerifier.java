package com.spindle.core.security.trust;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.resolve.ResolvedModSet;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public final class ArtifactSignatureVerifier {
  public void verify(ArtifactSignatureSidecar sidecar, ResolvedModSet.ResolvedMod mod)
      throws LoaderException, VerificationException {
    if (!mod.sha256().equals(sidecar.artifactSha256())) {
      throw new VerificationException(
          VerificationFailureKind.ARTIFACT_HASH_MISMATCH,
          "declares artifactSha256 `"
              + sidecar.artifactSha256()
              + "`, but the actual jar hash is `"
              + mod.sha256()
              + "`.");
    }
    if (!mod.id().equals(sidecar.signedFields().modId())
        || !mod.version().equals(sidecar.signedFields().version())) {
      throw new VerificationException(
          VerificationFailureKind.SIGNATURE_INVALID,
          "declares signedFields `"
              + sidecar.signedFields().modId()
              + "@"
              + sidecar.signedFields().version()
              + "`, but the resolved artifact is `"
              + mod.id()
              + "@"
              + mod.version()
              + "`.");
    }

    byte[] payload =
        payload(mod.sha256(), mod.id(), mod.version(), sidecar.signerId())
            .getBytes(StandardCharsets.UTF_8);
    PublicKey publicKey = decodePublicKey(sidecar);
    try {
      Signature signature = Signature.getInstance("Ed25519");
      signature.initVerify(publicKey);
      signature.update(payload);
      if (!signature.verify(sidecar.decodeSignature())) {
        throw new VerificationException(
            VerificationFailureKind.SIGNATURE_INVALID,
            "failed Ed25519 verification for signer `" + sidecar.signerId() + "`.");
      }
    } catch (VerificationException exception) {
      throw exception;
    } catch (GeneralSecurityException exception) {
      throw new LoaderException("Ed25519 verification support is unavailable", exception);
    }
  }

  public String payload(String artifactSha256, String modId, String version, String signerId) {
    return "spindle-artifact-signature-v1\n"
        + "artifactSha256="
        + artifactSha256
        + "\n"
        + "modId="
        + modId
        + "\n"
        + "version="
        + version
        + "\n"
        + "signerId="
        + signerId;
  }

  private PublicKey decodePublicKey(ArtifactSignatureSidecar sidecar)
      throws LoaderException, VerificationException {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
      return keyFactory.generatePublic(new X509EncodedKeySpec(sidecar.decodePublicKey()));
    } catch (InvalidKeySpecException | IllegalArgumentException exception) {
      throw new VerificationException(
          VerificationFailureKind.SIDECAR_INVALID,
          "contains an invalid Ed25519 public key.",
          exception);
    } catch (GeneralSecurityException exception) {
      throw new LoaderException("Ed25519 public key support is unavailable", exception);
    }
  }

  public enum VerificationFailureKind {
    SIDECAR_INVALID,
    ARTIFACT_HASH_MISMATCH,
    SIGNATURE_INVALID
  }

  public static final class VerificationException extends Exception {
    private final VerificationFailureKind kind;

    public VerificationException(VerificationFailureKind kind, String message) {
      super(message);
      this.kind = kind;
    }

    public VerificationException(VerificationFailureKind kind, String message, Throwable cause) {
      super(message, cause);
      this.kind = kind;
    }

    public VerificationFailureKind kind() {
      return kind;
    }
  }
}
