package com.mcmodloader.core.process;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JavaExecutableResolver {
    public Path resolve() throws LoaderException {
        Path javaHome = Path.of(System.getProperty("java.home", ""));
        String executableName = isWindows() ? "java.exe" : "java";
        Path candidate = javaHome.resolve("bin").resolve(executableName).toAbsolutePath().normalize();
        if (Files.isRegularFile(candidate)) {
            return candidate;
        }

        Path alternate = javaHome.resolve(executableName).toAbsolutePath().normalize();
        if (Files.isRegularFile(alternate)) {
            return alternate;
        }

        throw new LoaderException("Failed to resolve Java executable from current JVM home: " + javaHome);
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
