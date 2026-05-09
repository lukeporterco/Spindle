package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record MinecraftPlanFingerprint(String name, String sha256) {
    public static MinecraftPlanFingerprint fromFile(String name, Path path) throws LoaderException {
        try {
            return new MinecraftPlanFingerprint(name, sha256(Files.readAllBytes(path)));
        } catch (IOException exception) {
            throw new LoaderException("Failed to read plan fingerprint input " + path.toString().replace('\\', '/'), exception);
        }
    }

    public static String sha256(byte[] bytes) throws LoaderException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new LoaderException("SHA-256 algorithm unavailable", exception);
        }
    }
}
