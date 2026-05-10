package com.spindle.fixture.runtime;

import com.spindle.api.ModContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class RuntimeLifecycleFixtures {
  private RuntimeLifecycleFixtures() {}

  public static final class AlphaLifecycle {
    public static void bootstrap(ModContext context) throws Exception {
      append(context, "alpha|BOOTSTRAP");
      Files.writeString(
          context.generatedDirectory().resolve("alpha.marker"),
          "alpha-bootstrap\n",
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void preServerMain(ModContext context) throws Exception {
      append(context, "alpha|PRE_SERVER_MAIN");
    }
  }

  public static final class BetaLifecycle {
    public static void bootstrap(ModContext context) throws Exception {
      append(context, "beta|BOOTSTRAP");
      Files.writeString(
          context.generatedDirectory().resolve("beta.marker"),
          "beta-bootstrap\n",
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void configure(ModContext context) throws Exception {
      append(context, "beta|CONFIGURE");
    }
  }

  public static final class InvalidLifecycleHandler {
    public static void bootstrap(String ignored) {}
  }

  public static final class SecurityReportAwareLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      append(
          context,
          "report-exists="
              + Files.exists(context.workingDirectory().resolve("spindle.security-report.json")));
    }
  }

  public static final class CapabilityAwareLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      Files.writeString(
          context.generatedDirectory().resolve("capabilities.marker"),
          context.hasCapability("storage.generated")
              + "|"
              + context.grantedCapabilities().contains("storage.generated")
              + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public static final class DeniedStorageLifecycle {
    public static void bootstrap(ModContext context) {
      context.dataDirectory();
    }
  }

  public interface GreetingService {
    String greeting();
  }

  public interface AlternateGreetingService {
    String alternateGreeting();
  }

  public static final class GreetingServiceImpl implements GreetingService {
    public GreetingServiceImpl() {}

    @Override
    public String greeting() {
      return "hello-from-provider";
    }
  }

  public static final class GreetingServiceImplTwo implements GreetingService {
    public GreetingServiceImplTwo() {}

    @Override
    public String greeting() {
      return "hello-from-provider-two";
    }
  }

  public static final class GreetingConsumerLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      GreetingService greetingService =
          context.services().require("sample:greeting", GreetingService.class);
      Files.writeString(
          context.generatedDirectory().resolve("greeting.marker"),
          greetingService.greeting() + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public static final class OptionalGreetingConsumerLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      String marker =
          context.services().find("sample:greeting", GreetingService.class).isPresent()
              ? "present"
              : "missing";
      Files.writeString(
          context.generatedDirectory().resolve("optional-greeting.marker"),
          marker + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public static final class UndeclaredServiceConsumerLifecycle {
    public static void bootstrap(ModContext context) {
      context.services().require("sample:greeting", GreetingService.class);
    }
  }

  public static final class ConfigReaderLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      String marker =
          context.config().getBoolean("enabled")
              + "|"
              + context.config().getInteger("maxcount")
              + "|"
              + context.config().getNumber("scale")
              + "|"
              + context.config().getString("mode");
      Files.writeString(
          context.generatedDirectory().resolve("config.marker"),
          marker + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public static final class ConfigKeysLifecycle {
    public static void bootstrap(ModContext context) throws IOException {
      Files.writeString(
          context.generatedDirectory().resolve("config-keys.marker"),
          String.join("|", context.config().keys()) + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public static final class ConfigWriterLifecycle {
    public static void bootstrap(ModContext context) {
      context.config().setString("mode", "fast");
    }
  }

  public static final class UndeclaredConfigReaderLifecycle {
    public static void bootstrap(ModContext context) {
      context.config().getString("missing");
    }
  }

  private static void append(ModContext context, String line) throws IOException {
    Files.writeString(
        context.workingDirectory().resolve("lifecycle.log"),
        line + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
  }
}
