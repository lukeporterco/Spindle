package com.spindle.core.security.trust;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ArtifactSignatureReader {
  private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

  public ArtifactSignatureSidecar read(Path sidecarPath) throws SidecarReadException {
    try (Reader reader = Files.newBufferedReader(sidecarPath, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      int schemaVersion = requiredInt(root, "schemaVersion", sidecarPath);
      if (schemaVersion != 1) {
        throw new SidecarReadException(
            "uses unsupported schemaVersion `"
                + schemaVersion
                + "`. Spindle only supports schemaVersion `1` sidecars.");
      }

      String signatureKind = requiredString(root, "signatureKind", sidecarPath);
      if (!"spindle-ed25519".equals(signatureKind)) {
        throw new SidecarReadException(
            "uses unsupported signatureKind `"
                + signatureKind
                + "`. Spindle expects `spindle-ed25519`.");
      }

      String algorithm = requiredString(root, "algorithm", sidecarPath);
      if (!"Ed25519".equals(algorithm)) {
        throw new SidecarReadException(
            "uses unsupported algorithm `"
                + algorithm
                + "`. Spindle currently supports `Ed25519`.");
      }

      String signerId = requiredString(root, "signerId", sidecarPath);
      String publicKeyBase64 = requiredString(root, "publicKey", sidecarPath);
      String signatureBase64 = requiredString(root, "signature", sidecarPath);
      String artifactSha256 = requiredString(root, "artifactSha256", sidecarPath);
      if (!SHA256_PATTERN.matcher(artifactSha256).matches()) {
        throw new SidecarReadException("declares a non-hex artifactSha256 value.");
      }
      requireBase64(publicKeyBase64, "publicKey", sidecarPath);
      requireBase64(signatureBase64, "signature", sidecarPath);

      JsonObject signedFieldsObject = requiredObject(root, "signedFields", sidecarPath);
      ArtifactSignatureSidecar.SignedFields signedFields =
          new ArtifactSignatureSidecar.SignedFields(
              requiredString(signedFieldsObject, "modId", sidecarPath),
              requiredString(signedFieldsObject, "version", sidecarPath));

      return new ArtifactSignatureSidecar(
          schemaVersion,
          signatureKind,
          algorithm,
          signerId,
          publicKeyBase64,
          signatureBase64,
          artifactSha256.toLowerCase(Locale.ROOT),
          signedFields);
    } catch (SidecarReadException exception) {
      throw exception;
    } catch (JsonParseException | IllegalStateException exception) {
      throw new SidecarReadException("is malformed JSON.", exception);
    } catch (IOException exception) {
      throw new SidecarReadException("could not be read.", exception);
    }
  }

  private static JsonObject requiredObject(JsonObject root, String field, Path sidecarPath)
      throws SidecarReadException {
    if (!root.has(field) || !root.get(field).isJsonObject()) {
      throw new SidecarReadException("is missing object field `" + field + "`.");
    }
    return root.getAsJsonObject(field);
  }

  private static String requiredString(JsonObject root, String field, Path sidecarPath)
      throws SidecarReadException {
    if (!root.has(field)
        || !root.get(field).isJsonPrimitive()
        || !root.get(field).getAsJsonPrimitive().isString()) {
      throw new SidecarReadException("is missing string field `" + field + "`.");
    }
    String value = root.get(field).getAsString().trim();
    if (value.isEmpty()) {
      throw new SidecarReadException("has empty string field `" + field + "`.");
    }
    return value;
  }

  private static int requiredInt(JsonObject root, String field, Path sidecarPath)
      throws SidecarReadException {
    if (!root.has(field)
        || !root.get(field).isJsonPrimitive()
        || !root.get(field).getAsJsonPrimitive().isNumber()) {
      throw new SidecarReadException("is missing integer field `" + field + "`.");
    }
    try {
      return root.get(field).getAsInt();
    } catch (NumberFormatException exception) {
      throw new SidecarReadException("has invalid integer field `" + field + "`.", exception);
    }
  }

  private static void requireBase64(String value, String field, Path sidecarPath)
      throws SidecarReadException {
    try {
      Base64.getDecoder().decode(value);
    } catch (IllegalArgumentException exception) {
      throw new SidecarReadException("has invalid base64 in `" + field + "`.", exception);
    }
  }

  public static final class SidecarReadException extends Exception {
    public SidecarReadException(String message) {
      super(message);
    }

    public SidecarReadException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
