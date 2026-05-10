package com.spindle.core.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MinecraftReproducibilityChecker {
  private static final Pattern ISO_TIMESTAMP =
      Pattern.compile(
          "\"[A-Za-z0-9]*timestamp[A-Za-z0-9]*\"\\s*:\\s*\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z\"");
  private static final Pattern ABSOLUTE_PATH =
      Pattern.compile(
          "(?:(?<=^)|(?<=[\\s\"']))(?:[A-Za-z]:[\\\\/]|\\\\\\\\|/(?:Users|home|var|tmp)/)");

  public MinecraftReproducibilityCheck check(
      String milestoneName, List<ReportPair> reportPairs, boolean offlineNetworkUseDetected)
      throws LoaderException {
    List<MinecraftReproducibilityCheck.ComparedReport> comparedReports = new ArrayList<>();
    List<String> failures = new ArrayList<>();
    boolean byteForByteEqual = true;
    boolean timestampLeakageDetected = false;
    boolean nondeterministicOrderingDetected = false;
    boolean pathInstabilityDetected = false;

    for (ReportPair pair : reportPairs) {
      byte[] firstBytes = readBytes(pair.firstPath());
      byte[] secondBytes = readBytes(pair.secondPath());
      String firstText = new String(firstBytes, java.nio.charset.StandardCharsets.UTF_8);
      String secondText = new String(secondBytes, java.nio.charset.StandardCharsets.UTF_8);
      boolean bytesEqual = java.util.Arrays.equals(firstBytes, secondBytes);
      boolean semanticEquality = semanticallyEqual(firstText, secondText);
      List<String> pairFailures = new ArrayList<>();

      if (!bytesEqual) {
        byteForByteEqual = false;
        pairFailures.add("bytes differ");
        if (semanticEquality) {
          nondeterministicOrderingDetected = true;
          pairFailures.add("semantic JSON content matched but serialized bytes changed");
        }
      }
      if (containsTimestampLeakage(firstText) || containsTimestampLeakage(secondText)) {
        timestampLeakageDetected = true;
        pairFailures.add("timestamp-like content detected");
      }
      if (containsPathInstability(firstText) || containsPathInstability(secondText)) {
        pathInstabilityDetected = true;
        pairFailures.add("absolute or machine-specific path content detected");
      }
      if (!pairFailures.isEmpty()) {
        failures.add(pair.reportName() + ": " + String.join("; ", pairFailures));
      }

      comparedReports.add(
          new MinecraftReproducibilityCheck.ComparedReport(
              pair.reportName(),
              stableComparedPath(pair.firstPath()),
              stableComparedPath(pair.secondPath()),
              sha256(firstBytes),
              sha256(secondBytes),
              bytesEqual,
              semanticEquality,
              pairFailures));
    }

    if (offlineNetworkUseDetected) {
      failures.add("offline replay used network");
    }

    return new MinecraftReproducibilityCheck(
        1,
        milestoneName,
        comparedReports,
        byteForByteEqual,
        timestampLeakageDetected,
        nondeterministicOrderingDetected,
        pathInstabilityDetected,
        offlineNetworkUseDetected,
        failures);
  }

  private byte[] readBytes(Path path) throws LoaderException {
    try {
      return Files.readAllBytes(path);
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read reproducibility report " + path.toString().replace('\\', '/'), exception);
    }
  }

  private boolean semanticallyEqual(String first, String second) {
    try {
      JsonElement firstJson = JsonParser.parseString(first);
      JsonElement secondJson = JsonParser.parseString(second);
      return firstJson.equals(secondJson);
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private boolean containsTimestampLeakage(String content) {
    return ISO_TIMESTAMP.matcher(content).find();
  }

  private boolean containsPathInstability(String content) {
    return ABSOLUTE_PATH.matcher(content).find();
  }

  private String stableComparedPath(Path path) {
    String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
    return name.toLowerCase(Locale.ROOT);
  }

  private String sha256(byte[] bytes) throws LoaderException {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new LoaderException("SHA-256 algorithm unavailable", exception);
    }
  }

  public record ReportPair(String reportName, Path firstPath, Path secondPath) {}
}
