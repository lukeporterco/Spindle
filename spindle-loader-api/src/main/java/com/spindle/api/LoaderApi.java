package com.spindle.api;

/**
 * Public metadata for the stabilized runtime-facing Spindle Loader API.
 *
 * <p>This describes the Runtime API-0 boundary that mods program against today. It does not
 * stabilize target-specific APIs such as {@code com.spindle.api.minecraft}.
 */
public final class LoaderApi {
  public static final int RUNTIME_API_VERSION = 1;
  public static final String API_STATUS = "spindle-loader-runtime-api-stabilized";
  public static final String API_SCOPE = "runtime-facing-spindle-loader-api";
  public static final String TARGET_MODEL = "minecraft-as-target-not-foundation";
  public static final boolean SANDBOXED = false;
  public static final String SANDBOX_CLAIM = "not-sandboxed";

  private LoaderApi() {}
}
