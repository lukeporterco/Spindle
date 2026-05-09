package com.spindle.core.resolve;

final class VersionRequirement {
  private final String text;
  private final String minimumVersion;

  private VersionRequirement(String text, String minimumVersion) {
    this.text = text;
    this.minimumVersion = minimumVersion;
  }

  static VersionRequirement parse(String text) {
    if (text == null || !text.startsWith(">=")) {
      throw new IllegalArgumentException("Unsupported requirement");
    }

    String minimumVersion = text.substring(2).trim();
    if (!isSupportedVersion(minimumVersion)) {
      throw new IllegalArgumentException("Unsupported requirement");
    }
    return new VersionRequirement(text, minimumVersion);
  }

  boolean matches(String actualVersion) {
    return compareVersions(actualVersion, minimumVersion) >= 0;
  }

  String text() {
    return text;
  }

  static boolean isSupportedVersion(String version) {
    String[] segments = version.split("\\.");
    if (segments.length < 1 || segments.length > 3) {
      return false;
    }
    for (String segment : segments) {
      if (segment.isEmpty() || !segment.chars().allMatch(Character::isDigit)) {
        return false;
      }
    }
    return true;
  }

  private static int compareVersions(String left, String right) {
    String[] leftParts = left.split("\\.");
    String[] rightParts = right.split("\\.");
    int size = Math.max(leftParts.length, rightParts.length);
    for (int index = 0; index < size; index++) {
      int leftValue = index < leftParts.length ? Integer.parseInt(leftParts[index]) : 0;
      int rightValue = index < rightParts.length ? Integer.parseInt(rightParts[index]) : 0;
      int result = Integer.compare(leftValue, rightValue);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }
}
