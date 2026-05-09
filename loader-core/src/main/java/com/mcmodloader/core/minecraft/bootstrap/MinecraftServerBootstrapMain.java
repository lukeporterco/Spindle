package com.mcmodloader.core.minecraft.bootstrap;

import com.mcmodloader.core.diagnostics.LoaderException;

public final class MinecraftServerBootstrapMain {
    private MinecraftServerBootstrapMain() {
    }

    public static void main(String[] args) {
        int exitCode = MinecraftBootstrapExitCode.SUCCESS.code();
        try {
            MinecraftBootstrapArguments arguments = MinecraftBootstrapArguments.parse(args);
            exitCode = new MinecraftBootstrapRunner().run(arguments).exitCode();
        } catch (LoaderException exception) {
            System.err.println("[loader] bootstrap error: " + exception.getMessage());
            exitCode = MinecraftBootstrapExitCode.BOOTSTRAP_FAILURE.code();
        } catch (Exception exception) {
            System.err.println("[loader] bootstrap error: unexpected failure");
            exception.printStackTrace(System.err);
            exitCode = MinecraftBootstrapExitCode.BOOTSTRAP_FAILURE.code();
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
