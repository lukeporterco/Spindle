package com.mcmodloader.core.discovery;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class ModDiscoverer {
    public List<ModCandidate> discover(LaunchContext context) throws LoaderException {
        Path modsDirectory = context.modsDirectory();
        if (!Files.isDirectory(modsDirectory)) {
            return List.of();
        }

        List<ModCandidate> candidates = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDirectory, "*.jar")) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path) || isHidden(path) || !containsMetadata(path)) {
                    continue;
                }

                Path relativePath = context.workingDirectory().relativize(path.toAbsolutePath().normalize());
                candidates.add(new ModCandidate(path.toAbsolutePath().normalize(), relativePath, sha256(path)));
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to discover mods in " + modsDirectory, exception);
        }

        candidates.sort(Comparator.comparing(ModCandidate::normalizedRelativePath));
        return List.copyOf(candidates);
    }

    private boolean isHidden(Path path) throws IOException {
        return Files.isHidden(path) || path.getFileName().toString().startsWith(".");
    }

    private boolean containsMetadata(Path path) throws IOException {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            ZipEntry entry = jarFile.getEntry("loader.mod.json");
            return entry != null && !entry.isDirectory();
        }
    }

    private String sha256(Path path) throws LoaderException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new LoaderException("Failed to hash mod jar " + path, exception);
        }
    }
}
