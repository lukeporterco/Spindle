package com.mcmodloader.core.lockfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class LockfileVerifier {
    private final Gson gson = new GsonBuilder().create();

    public boolean exists(Path lockfilePath) {
        return Files.isRegularFile(lockfilePath);
    }

    public void verify(Path lockfilePath, LaunchContext context, ResolvedModSet resolvedModSet) throws LoaderException {
        Lockfile actual = read(lockfilePath);
        validate(actual);
        Lockfile expected = Lockfile.from(context, resolvedModSet);

        if (actual.schema() != expected.schema()) {
            throw new LoaderException("loader.lock.json schema mismatch");
        }
        if (!actual.loader().equals(expected.loader())) {
            throw new LoaderException("loader.lock.json loader version mismatch");
        }
        if (actual.javaMajorVersion() != expected.javaMajorVersion()) {
            throw new LoaderException("loader.lock.json Java version mismatch");
        }
        if (!actual.minecraftVersion().equals(expected.minecraftVersion())) {
            throw new LoaderException("loader.lock.json Minecraft version mismatch");
        }

        List<Lockfile.LockedMod> actualMods = actual.mods();
        List<Lockfile.LockedMod> expectedMods = expected.mods();
        if (actualMods.size() != expectedMods.size()) {
            throw new LoaderException("loader.lock.json selected mod set mismatch");
        }

        for (int index = 0; index < expectedMods.size(); index++) {
            Lockfile.LockedMod actualMod = actualMods.get(index);
            Lockfile.LockedMod expectedMod = expectedMods.get(index);
            if (!actualMod.id().equals(expectedMod.id())) {
                throw new LoaderException("loader.lock.json mod id mismatch at index " + index);
            }
            if (!actualMod.version().equals(expectedMod.version())) {
                throw new LoaderException("loader.lock.json version mismatch for mod " + expectedMod.id());
            }
            if (!actualMod.path().equals(expectedMod.path())) {
                throw new LoaderException("loader.lock.json path mismatch for mod " + expectedMod.id());
            }
            if (!actualMod.sha256().equals(expectedMod.sha256())) {
                throw new LoaderException("loader.lock.json sha256 mismatch for mod " + expectedMod.id());
            }
        }
    }

    private Lockfile read(Path lockfilePath) throws LoaderException {
        try (Reader reader = Files.newBufferedReader(lockfilePath, StandardCharsets.UTF_8)) {
            Lockfile lockfile = gson.fromJson(reader, Lockfile.class);
            if (lockfile == null) {
                throw new LoaderException("loader.lock.json is empty");
            }
            return lockfile;
        } catch (JsonParseException exception) {
            throw new LoaderException("loader.lock.json is invalid", exception);
        } catch (RuntimeException exception) {
            throw new LoaderException("loader.lock.json is invalid", exception);
        } catch (IOException exception) {
            throw new LoaderException("Failed to read loader.lock.json", exception);
        }
    }

    private void validate(Lockfile lockfile) throws LoaderException {
        if (lockfile.mods() == null) {
            throw new LoaderException("loader.lock.json mods is missing");
        }
        if (lockfile.loader() == null) {
            throw new LoaderException("loader.lock.json loader is missing");
        }
        if (lockfile.minecraftVersion() == null) {
            throw new LoaderException("loader.lock.json Minecraft version is missing");
        }

        List<Lockfile.LockedMod> mods = lockfile.mods();
        for (int index = 0; index < mods.size(); index++) {
            Lockfile.LockedMod mod = mods.get(index);
            if (mod == null) {
                throw new LoaderException("loader.lock.json mod is missing at index " + index);
            }
            if (Objects.requireNonNullElse(mod.id(), "").isEmpty()) {
                throw new LoaderException("loader.lock.json mod id is missing at index " + index);
            }
            if (Objects.requireNonNullElse(mod.version(), "").isEmpty()) {
                throw new LoaderException("loader.lock.json mod version is missing at index " + index);
            }
            if (Objects.requireNonNullElse(mod.path(), "").isEmpty()) {
                throw new LoaderException("loader.lock.json mod path is missing at index " + index);
            }
            if (Objects.requireNonNullElse(mod.sha256(), "").isEmpty()) {
                throw new LoaderException("loader.lock.json mod sha256 is missing at index " + index);
            }
        }
    }
}
