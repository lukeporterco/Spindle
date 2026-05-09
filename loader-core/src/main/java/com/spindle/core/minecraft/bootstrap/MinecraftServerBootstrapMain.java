package com.spindle.core.minecraft.bootstrap;

import com.spindle.core.diagnostics.LoaderException;

public final class MinecraftServerBootstrapMain {
  private MinecraftServerBootstrapMain() {}

  public static void main(String[] args) {
    int exitCode = MinecraftBootstrapExitCode.SUCCESS.code();
    try {
      MinecraftBootstrapArguments arguments = MinecraftBootstrapArguments.parse(args);
      exitCode = new MinecraftBootstrapRunner().run(arguments).exitCode();
    } catch (LoaderException exception) {
      System.err.println("[spindle] bootstrap error: " + exception.getMessage());
      exitCode = MinecraftBootstrapExitCode.BOOTSTRAP_FAILURE.code();
    } catch (Exception exception) {
      System.err.println("[spindle] bootstrap error: unexpected failure");
      exception.printStackTrace(System.err);
      exitCode = MinecraftBootstrapExitCode.BOOTSTRAP_FAILURE.code();
    }
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }
}
