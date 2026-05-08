package com.mcmodloader.core.artifact;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MinecraftArtifactVerifier {
    public VerificationResult verify(Path path, String expectedSha1, Long expectedSize) throws LoaderException {
        if (!Files.isRegularFile(path)) {
            throw new LoaderException("Artifact file does not exist: " + path);
        }

        Hashes hashes = computeHashes(path);
        if (expectedSha1 != null && !expectedSha1.isBlank() && !expectedSha1.equalsIgnoreCase(hashes.sha1())) {
            throw new LoaderException(
                "Artifact SHA-1 mismatch for " + path.toString().replace('\\', '/') + ": expected " + expectedSha1 + " but was " + hashes.sha1()
            );
        }
        if (expectedSize != null && expectedSize.longValue() >= 0L && expectedSize.longValue() != hashes.size()) {
            throw new LoaderException(
                "Artifact size mismatch for " + path.toString().replace('\\', '/') + ": expected " + expectedSize + " but was " + hashes.size()
            );
        }

        boolean verified = hasExpectations(expectedSha1, expectedSize);
        return new VerificationResult(hashes.sha1(), hashes.sha256(), hashes.size(), verified);
    }

    private boolean hasExpectations(String expectedSha1, Long expectedSize) {
        return (expectedSha1 != null && !expectedSha1.isBlank()) || expectedSize != null;
    }

    private Hashes computeHashes(Path path) throws LoaderException {
        try {
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            long size = 0L;
            byte[] buffer = new byte[8192];
            try (InputStream inputStream = Files.newInputStream(path)) {
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    if (read == 0) {
                        continue;
                    }
                    sha1Digest.update(buffer, 0, read);
                    sha256Digest.update(buffer, 0, read);
                    size += read;
                }
            }
            return new Hashes(toHex(sha1Digest), toHex(sha256Digest), size);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new LoaderException("Failed to verify artifact " + path.toString().replace('\\', '/'), exception);
        }
    }

    private String toHex(MessageDigest digest) {
        return HexFormat.of().formatHex(digest.digest());
    }

    private record Hashes(String sha1, String sha256, long size) {
    }

    public record VerificationResult(String sha1, String sha256, long size, boolean verified) {
    }
}
