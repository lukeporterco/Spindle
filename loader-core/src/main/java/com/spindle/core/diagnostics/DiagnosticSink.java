package com.spindle.core.diagnostics;

import java.io.IOException;

public interface DiagnosticSink {
  void record(DiagnosticEvent event);

  void write() throws IOException;
}
