package com.spindle.core.security.trust;

import java.util.Base64;

public record ArtifactSignatureSidecar(
    int schemaVersion,
    String signatureKind,
    String algorithm,
    String signerId,
    String publicKeyBase64,
    String signatureBase64,
    String artifactSha256,
    SignedFields signedFields) {
  public ArtifactSignatureSidecar {
    signatureKind = normalize(signatureKind);
    algorithm = normalize(algorithm);
    signerId = normalize(signerId);
    publicKeyBase64 = normalize(publicKeyBase64);
    signatureBase64 = normalize(signatureBase64);
    artifactSha256 = normalize(artifactSha256);
  }

  public byte[] decodePublicKey() {
    return Base64.getDecoder().decode(publicKeyBase64);
  }

  public byte[] decodeSignature() {
    return Base64.getDecoder().decode(signatureBase64);
  }

  public record SignedFields(String modId, String version) {
    public SignedFields {
      modId = normalize(modId);
      version = normalize(version);
    }
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
