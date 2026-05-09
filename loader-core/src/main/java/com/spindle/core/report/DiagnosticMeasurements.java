package com.spindle.core.report;

import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchPhase;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class DiagnosticMeasurements {
  private DiagnosticMeasurements() {}

  public static Map<String, String> details(String... values) {
    Map<String, String> details = new LinkedHashMap<>();
    for (int index = 0; index + 1 < values.length; index += 2) {
      String value = values[index + 1];
      if (value != null && !value.isBlank()) {
        details.put(values[index], value);
      }
    }
    return details;
  }

  public static <T> T measure(
      DiagnosticSink sink,
      String name,
      LaunchPhase phase,
      ThrowingSupplier<T> supplier,
      Function<T, Map<String, String>> detailsFactory)
      throws LoaderException {
    long start = System.nanoTime();
    try {
      T result = supplier.get();
      sink.record(
          new DiagnosticEvent(
              name, phase.name(), elapsedMillis(start), "ok", null, detailsFactory.apply(result)));
      return result;
    } catch (LoaderException exception) {
      sink.record(
          new DiagnosticEvent(
              name, phase.name(), elapsedMillis(start), "error", exception.getMessage(), null));
      throw new LoaderException(
          "Failure during " + phase.name() + ": " + exception.getMessage(), exception);
    } catch (Exception exception) {
      sink.record(
          new DiagnosticEvent(
              name,
              phase.name(),
              elapsedMillis(start),
              "error",
              "Unexpected failure during " + name,
              null));
      throw new LoaderException("Unexpected failure during " + name, exception);
    }
  }

  public static long elapsedMillis(long start) {
    return Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
  }

  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T get() throws Exception;
  }
}
