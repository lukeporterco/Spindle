package com.mcmodloader.core.minecraft;

import com.mcmodloader.api.minecraft.MinecraftServerModInitializer;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.metadata.ModMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

public final class MinecraftModExecutionPlanner {
    private static final String MILESTONE_NAME = "Milestone 8";
    private final MinecraftJarScanner jarScanner = new MinecraftJarScanner();
    private final MinecraftProtectedPackagePolicy protectedPackagePolicy = new MinecraftProtectedPackagePolicy();

    public MinecraftModExecutionPlan plan(
        LaunchContext context,
        MinecraftProviderConfig config,
        List<ModCandidate> parsedCandidates,
        MinecraftServerRuntimePlan runtimePlan,
        MinecraftRuntimeBoundary runtimeBoundary,
        MinecraftModIntegrationPlan integrationPlan,
        MinecraftPlanFingerprint runtimePlanFingerprint,
        MinecraftPlanFingerprint boundaryFingerprint,
        MinecraftPlanFingerprint integrationPlanFingerprint
    ) throws LoaderException {
        MinecraftClassLoaderPolicy classLoaderPolicy =
            MinecraftClassLoaderPolicy.strictDefault(protectedPackagePolicy, config.denyLoaderInternals());
        Set<String> acceptedIntegrationMods = integrationPlan.acceptedMods().stream().map(MinecraftModAcceptance::modId).collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        List<MinecraftExecutableMod> executableMods = new ArrayList<>();
        List<MinecraftEntrypointPlan> executableEntrypoints = new ArrayList<>();
        List<MinecraftModRejection> rejectedMods = new ArrayList<>(integrationPlan.rejectedMods());

        for (ModCandidate candidate : parsedCandidates.stream().sorted(Comparator.comparing(ModCandidate::normalizedRelativePath)).toList()) {
            ModMetadata metadata = candidate.metadata();
            if (!acceptedIntegrationMods.contains(metadata.id())) {
                continue;
            }
            MinecraftJarScanResult scan = jarScanner.scan(candidate.jarPath(), candidate.normalizedRelativePath());
            String modId = metadata.id();
            String modClassLoaderId = "minecraft-mod:" + modId;
            List<String> minecraftServerEntrypoints = metadata.minecraftServerEntrypoints();
            List<String> protectedDefinitions = scan.packages().stream().filter(protectedPackagePolicy::isProtectedDefinitionPackage).sorted().toList();
            List<String> executionFailures = new ArrayList<>();
            if (!candidate.jarPath().toAbsolutePath().normalize().startsWith(context.modsDirectory())) {
                executionFailures.add("mod jar path escapes allowed mods directory");
            }
            if (!scan.suspiciousPaths().isEmpty()) {
                executionFailures.add("mod jar contains traversal or absolute entries");
            }
            if (!protectedDefinitions.isEmpty()) {
                executionFailures.add("mod defines protected packages: " + String.join(",", protectedDefinitions));
            }
            if (!"server".equals(metadata.side()) && !"universal".equals(metadata.side())) {
                executionFailures.add("mod side is incompatible with server bootstrap");
            }
            if (candidate.sha256() == null || candidate.sha256().isBlank()) {
                executionFailures.add("mod jar hash is missing");
            }
            if (!scan.unsupportedClassFiles().isEmpty()) {
                executionFailures.add("mod contains unsupported class files");
            }
            if (minecraftServerEntrypoints.isEmpty()) {
                executionFailures.add("missing minecraftServer entrypoint");
            }
            for (String entrypointClassName : minecraftServerEntrypoints) {
                if (!isValidClassName(entrypointClassName)) {
                    executionFailures.add("malformed minecraftServer entrypoint declaration");
                    break;
                }
            }
            if (!requirementMatches(metadata.depends().get("loader"), context.loaderVersion())) {
                executionFailures.add("loader dependency fails");
            }
            if (!requirementMatches(metadata.depends().get("java"), Integer.toString(context.javaMajorVersion()))) {
                executionFailures.add("java dependency fails");
            }
            if (!requirementMatches(metadata.depends().get("minecraft"), runtimePlan.resolvedMinecraftVersion())) {
                executionFailures.add("minecraft dependency fails");
            }

            if (!executionFailures.isEmpty()) {
                rejectedMods.add(new MinecraftModRejection(modId, String.join("; ", executionFailures), MinecraftBoundarySeverity.FATAL, true));
                continue;
            }

            long jarSize = fileSize(candidate.jarPath());
            List<MinecraftEntrypointDeclaration> entrypointDeclarations =
                minecraftServerEntrypoints.stream()
                    .sorted()
                    .map(className -> new MinecraftEntrypointDeclaration("minecraftServer", className))
                    .toList();
            executableMods.add(
                new MinecraftExecutableMod(
                    modId,
                    metadata.version(),
                    metadata.side(),
                    relativize(context.workingDirectory(), candidate.jarPath()),
                    candidate.sha256(),
                    jarSize,
                    entrypointDeclarations,
                    modClassLoaderId,
                    "minecraft-runtime",
                    classLoaderPolicy.protectedPackages(),
                    classLoaderPolicy.deniedPackages(),
                    classLoaderPolicy.allowedApiPackages(),
                    classLoaderPolicy.delegationPolicy()
                )
            );
            for (String entrypointClassName : minecraftServerEntrypoints.stream().sorted().toList()) {
                executableEntrypoints.add(
                    new MinecraftEntrypointPlan(
                        modId,
                        "minecraftServer",
                        entrypointClassName,
                        MinecraftServerModInitializer.class.getName(),
                        "onInitializeMinecraftServer",
                        relativize(context.workingDirectory(), candidate.jarPath()),
                        candidate.sha256(),
                        modClassLoaderId,
                        "minecraft-runtime"
                    )
                );
            }
        }

        if (config.strictExecution()) {
            List<String> warnings = new ArrayList<>(integrationPlan.warnings());
            warnings.addAll(
                integrationPlan.issues().stream().filter(issue -> issue.severity() == MinecraftBoundarySeverity.WARNING).map(MinecraftBoundaryViolation::reason).toList()
            );
            if (!warnings.isEmpty()) {
                throw new LoaderException("Strict Minecraft execution failed because warnings were promoted to fatal: " + String.join("; ", warnings));
            }
        }

        Set<String> seenIds = new TreeSet<>();
        for (MinecraftExecutableMod executableMod : executableMods) {
            if (!seenIds.add(executableMod.modId())) {
                throw new LoaderException("Duplicate executable mod id in execution plan: " + executableMod.modId());
            }
        }

        List<String> runtimeClasspathSummary =
            runtimePlan.classpathEntries().stream().map(MinecraftServerRuntimeClasspath.Entry::path).sorted().toList();
        MinecraftExecutionProof proof =
            new MinecraftExecutionProof(
                !executableMods.isEmpty(),
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
            );
        return new MinecraftModExecutionPlan(
            1,
            MILESTONE_NAME,
            runtimePlan.resolvedMinecraftVersion(),
            Integer.toString(context.javaMajorVersion()),
            config.side().id(),
            runtimePlanFingerprint,
            boundaryFingerprint,
            integrationPlanFingerprint,
            executableMods.stream().sorted(Comparator.comparing(MinecraftExecutableMod::modId)).toList(),
            rejectedMods.stream().sorted(Comparator.comparing(MinecraftModRejection::candidate)).toList(),
            executableEntrypoints.stream().sorted(Comparator.comparing(MinecraftEntrypointPlan::entrypointClassName)).toList(),
            classLoaderPolicy,
            resolveMinecraftMainClass(context, runtimePlan),
            runtimePlan.serverArgs(),
            runtimeClasspathSummary,
            new MinecraftExecutionPolicy(
                config.strictExecution(),
                config.denyLoaderInternals(),
                config.verifyPlanFingerprints(),
                config.bootstrapOffline(),
                config.bootstrapFakeServer()
            ),
            proof
        );
    }

    private boolean requirementMatches(String requirement, String actualVersion) {
        if (requirement == null || requirement.isBlank()) {
            return true;
        }
        if (!requirement.startsWith(">=")) {
            return requirement.equals(actualVersion);
        }
        return compareVersions(actualVersion, requirement.substring(2).trim()) >= 0;
    }

    private int compareVersions(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int size = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < size; index++) {
            int leftValue = parsePart(leftParts, index);
            int rightValue = parsePart(rightParts, index);
            int result = Integer.compare(leftValue, rightValue);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private int parsePart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        String digits = parts[index].chars().takeWhile(Character::isDigit).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    private long fileSize(Path path) throws LoaderException {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read mod jar size for " + path.toString().replace('\\', '/'), exception);
        }
    }

    private String resolveMinecraftMainClass(LaunchContext context, MinecraftServerRuntimePlan runtimePlan) throws LoaderException {
        if (runtimePlan.mainClass() != null && !runtimePlan.mainClass().isBlank()) {
            return runtimePlan.mainClass();
        }
        Path serverJarPath = Path.of(runtimePlan.serverJarPath());
        Path resolvedPath = serverJarPath.isAbsolute() ? serverJarPath : context.workingDirectory().resolve(serverJarPath);
        try (JarFile jarFile = new JarFile(resolvedPath.toFile())) {
            if (jarFile.getManifest() != null && jarFile.getManifest().getMainAttributes().getValue("Main-Class") != null) {
                return jarFile.getManifest().getMainAttributes().getValue("Main-Class").trim();
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to resolve Minecraft server main class from " + resolvedPath.toString().replace('\\', '/'), exception);
        }
        throw new LoaderException("Minecraft execution plan requires a server main class");
    }

    private boolean isValidClassName(String className) {
        return className.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+");
    }

    private String relativize(Path workingDirectory, Path path) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        try {
            return normalizedWorkingDirectory.relativize(normalizedPath).toString().replace('\\', '/');
        } catch (IllegalArgumentException exception) {
            return normalizedPath.toString().replace('\\', '/');
        }
    }
}
