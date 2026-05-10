package com.spindle.api.exception;

/** Base unchecked exception for stable Runtime API-0 failures raised through {@code loader-api}. */
public class SpindleApiException extends RuntimeException {
  public SpindleApiException(String message) {
    super(message);
  }

  public SpindleApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
