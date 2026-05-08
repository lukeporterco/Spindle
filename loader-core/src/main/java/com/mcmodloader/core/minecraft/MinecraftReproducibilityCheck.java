package com.mcmodloader.core.minecraft;

import java.util.List;

public record MinecraftReproducibilityCheck(
    int schema,
    String milestoneName,
    List<String> comparedReports,
    boolean byteForByteEqual,
    boolean timestampLeakageDetected,
    boolean nondeterministicOrderingDetected,
    boolean offlineNetworkUseDetected,
    List<String> failures
) {
    public MinecraftReproducibilityCheck {
        comparedReports = List.copyOf(comparedReports);
        failures = List.copyOf(failures);
    }
}
