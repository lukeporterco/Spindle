package com.spindle.sampleruntimemod;

import com.spindle.api.ModContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class SampleRuntimeLifecycle {
  private SampleRuntimeLifecycle() {}

  public static void bootstrap(ModContext context) throws Exception {
    Files.writeString(
        context.generatedDirectory().resolve("runtime-kernel.marker"),
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
    Files.writeString(
        context.dataDirectory().resolve("runtime-kernel.log"),
        "CONFIGURE" + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
  }

  public static void preServerMain(ModContext context) throws Exception {
    Files.writeString(
        context.dataDirectory().resolve("runtime-kernel.log"),
        "PRE_SERVER_MAIN" + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
  }
}
