package com.mcmodloader.core.report;

import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.profile.StartupProfile;
import com.mcmodloader.core.profile.StartupProfileWriter;
import java.nio.file.Path;

public final class StartupProfileSupport {
  private StartupProfileSupport() {}

  public static void writeStartupProfile(LaunchContext context, DiagnosticSink diagnosticSink)
      throws LoaderException {
    if (!(diagnosticSink instanceof JsonDiagnosticSink jsonDiagnosticSink)) {
      return;
    }

    Path startupProfilePath =
        context.workingDirectory().resolve("diagnostics/startup-profile.json");
    StartupProfile profile = StartupProfile.from(jsonDiagnosticSink.events());
    new StartupProfileWriter().write(startupProfilePath, profile);
    diagnosticSink.record(
        new DiagnosticEvent(
            "startup_profile.write",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Startup profile written",
            DiagnosticMeasurements.details(
                "startupProfileOutputPath",
                DisplayPaths.displayPath(context, startupProfilePath))));
  }
}
