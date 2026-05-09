package com.spindle.core.minecraft.bootstrap;

public enum MinecraftBootstrapExitCode {
  SUCCESS(0),
  PLAN_DRIFT(20),
  MOD_ENTRYPOINT_FAILURE(21),
  MINECRAFT_MAIN_FAILURE(22),
  BOOTSTRAP_FAILURE(23);

  private final int code;

  MinecraftBootstrapExitCode(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }
}
