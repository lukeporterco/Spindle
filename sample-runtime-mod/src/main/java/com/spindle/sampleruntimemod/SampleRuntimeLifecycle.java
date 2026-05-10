package com.spindle.sampleruntimemod;

import com.spindle.api.ModContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class SampleRuntimeLifecycle {
  private SampleRuntimeLifecycle() {}

  public static void bootstrap(ModContext context) throws Exception {
    context.requireCapability("storage.generated");
    Files.writeString(
        context.generatedDirectory().resolve("sample-runtime.marker"),
        context.modId()
            + "|"
            + context.modVersion()
            + "|"
            + context.side()
            + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  public static void configure(ModContext context) throws Exception {
    appendPhase(context, "CONFIGURE");
  }

  public static void preServerMain(ModContext context) throws Exception {
    appendPhase(context, "PRE_SERVER_MAIN");
  }

  private static void appendPhase(ModContext context, String phase) throws Exception {
    context.requireCapability("storage.data");
    Files.writeString(
        context.dataDirectory().resolve("lifecycle.log"),
        phase + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
  }
}
