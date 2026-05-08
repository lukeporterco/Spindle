package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;

public final class MinecraftVersionSelector {
    public MinecraftVersionSelection select(String requestedVersion, MinecraftVersionManifest manifest, boolean explicitVersionJsonSupplied)
        throws LoaderException {
        if (requestedVersion == null || requestedVersion.isBlank()) {
            throw new LoaderException("Missing requested Minecraft version.");
        }
        if ("latest-release".equals(requestedVersion)) {
            if (manifest == null) {
                throw new LoaderException("Minecraft version selection requires a version manifest for latest-release.");
            }
            String resolved = manifest.latestRelease();
            if (resolved == null || resolved.isBlank()) {
                throw unresolved(requestedVersion, manifest);
            }
            return new MinecraftVersionSelection(requestedVersion, resolved, "manifest");
        }
        if ("latest-snapshot".equals(requestedVersion)) {
            if (manifest == null) {
                throw new LoaderException("Minecraft version selection requires a version manifest for latest-snapshot.");
            }
            String resolved = manifest.latestSnapshot();
            if (resolved == null || resolved.isBlank()) {
                throw unresolved(requestedVersion, manifest);
            }
            return new MinecraftVersionSelection(requestedVersion, resolved, "manifest");
        }

        if (manifest == null) {
            if (explicitVersionJsonSupplied) {
                return new MinecraftVersionSelection(requestedVersion, requestedVersion, "explicit-version-json");
            }
            throw new LoaderException("Minecraft version selection requires a version manifest for " + requestedVersion + ".");
        }

        if (explicitVersionJsonSupplied) {
            return new MinecraftVersionSelection(requestedVersion, requestedVersion, "explicit-version-json");
        }
        if (manifest.findVersion(requestedVersion).isPresent()) {
            return new MinecraftVersionSelection(requestedVersion, requestedVersion, "manifest");
        }
        throw unresolved(requestedVersion, manifest);
    }

    private LoaderException unresolved(String requestedVersion, MinecraftVersionManifest manifest) {
        String latestRelease = manifest == null || manifest.latestRelease() == null || manifest.latestRelease().isBlank()
            ? "unknown"
            : manifest.latestRelease();
        String latestSnapshot = manifest == null || manifest.latestSnapshot() == null || manifest.latestSnapshot().isBlank()
            ? "unknown"
            : manifest.latestSnapshot();
        int versionCount = manifest == null ? 0 : manifest.versions().size();
        return new LoaderException(
            "Unable to resolve Minecraft version '" +
            requestedVersion +
            "'. Latest release: " +
            latestRelease +
            ". Latest snapshot: " +
            latestSnapshot +
            ". Manifest version count: " +
            versionCount +
            "."
        );
    }
}
