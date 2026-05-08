package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public final class MinecraftBundledServerInspector {
    public Inspection inspect(Path serverJar) throws LoaderException {
        if (serverJar == null) {
            return new Inspection(false, null, List.of(), List.of());
        }
        try (JarFile jarFile = new JarFile(serverJar.toFile())) {
            List<BundledEntry> libraries = readList(jarFile, "META-INF/libraries.list", "library");
            List<BundledEntry> versions = readList(jarFile, "META-INF/versions.list", "version");
            String mainClass = readMainClass(jarFile);
            boolean bundled = !libraries.isEmpty() || !versions.isEmpty();
            return new Inspection(bundled, mainClass, versions, libraries);
        } catch (IOException exception) {
            throw new LoaderException("Failed to inspect Minecraft server jar " + serverJar, exception);
        }
    }

    private List<BundledEntry> readList(JarFile jarFile, String entryName, String kind) throws IOException, LoaderException {
        ZipEntry entry = jarFile.getEntry(entryName);
        if (entry == null) {
            return List.of();
        }
        if (entry.isDirectory()) {
            throw new LoaderException("Bundled server metadata entry is a directory: " + entryName);
        }

        String content;
        try (var inputStream = jarFile.getInputStream(entry)) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<BundledEntry> entries = new ArrayList<>();
        int lineNumber = 0;
        for (String rawLine : content.split("\\R")) {
            lineNumber++;
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length < 3) {
                throw new LoaderException("Malformed bundled server metadata in " + entryName + " at line " + lineNumber);
            }
            String sha1 = parts[0];
            String id = parts[1];
            String path = parts[2].replace('\\', '/');
            if (path.startsWith("/") || path.contains("../") || path.contains("/..")) {
                throw new LoaderException("Unsafe bundled server path in " + entryName + ": " + path);
            }
            String jarEntryName = "library".equals(kind) ? "META-INF/libraries/" + path : "META-INF/versions/" + path;
            if (jarFile.getEntry(jarEntryName) == null) {
                throw new LoaderException("Bundled server metadata references missing entry " + jarEntryName);
            }
            entries.add(new BundledEntry(kind, id, sha1, path, jarEntryName));
        }
        entries.sort(Comparator.comparing(BundledEntry::kind).thenComparing(BundledEntry::path).thenComparing(BundledEntry::id));
        return List.copyOf(entries);
    }

    private String readMainClass(JarFile jarFile) throws IOException {
        ZipEntry mainClassEntry = jarFile.getEntry("META-INF/main-class");
        if (mainClassEntry != null && !mainClassEntry.isDirectory()) {
            try (var inputStream = jarFile.getInputStream(mainClassEntry)) {
                String value = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return null;
        }
        return manifest.getMainAttributes().getValue("Main-Class");
    }

    public record Inspection(boolean bundled, String mainClass, List<BundledEntry> versions, List<BundledEntry> libraries) {
        public Inspection {
            versions = List.copyOf(versions);
            libraries = List.copyOf(libraries);
        }
    }

    public record BundledEntry(String kind, String id, String sha1, String path, String jarEntryName) {
    }
}
