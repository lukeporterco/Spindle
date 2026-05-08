package com.mcmodloader.core.minecraft;

import java.util.List;

public record MinecraftRuntimeProvenance(
    int schema,
    String milestoneName,
    List<String> inputs,
    String cacheSource,
    String networkSource,
    boolean offlineReplay,
    boolean strict,
    String commandLineMode,
    String workingDirectory,
    String outputDirectory,
    List<String> reportDependencies
) {
    public MinecraftRuntimeProvenance {
        inputs = List.copyOf(inputs);
        reportDependencies = List.copyOf(reportDependencies);
    }
}
