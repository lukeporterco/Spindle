package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MinecraftRuntimeFileVerifier {
    public Verification verify(Path path, String expectedSha1) throws LoaderException {
        if (path == null || !Files.isRegularFile(path)) {
            return new Verification(null, null, 0L, false, false, "missing");
        }
        try {
            String sha1 = digest(path, "SHA-1");
            String sha256 = digest(path, "SHA-256");
            long size = Files.size(path);
            boolean verified = expectedSha1 == null || expectedSha1.isBlank() || expectedSha1.equalsIgnoreCase(sha1);
            return new Verification(sha1, sha256, size, true, verified, verified ? "verified" : "sha1-mismatch");
        } catch (IOException exception) {
            throw new LoaderException("Failed to verify Minecraft runtime file " + path, exception);
        }
    }

    private String digest(Path path, String algorithm) throws LoaderException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream inputStream = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new LoaderException("Failed to hash Minecraft runtime file " + path, exception);
        }
    }

    public record Verification(String sha1, String sha256, long size, boolean present, boolean verified, String status) {
    }
}
