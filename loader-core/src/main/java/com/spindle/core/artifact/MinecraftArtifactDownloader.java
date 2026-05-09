package com.spindle.core.artifact;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

public final class MinecraftArtifactDownloader {
  private final HttpClient httpClient;
  private final MinecraftArtifactVerifier verifier;
  private final Duration connectTimeout;
  private final MinecraftNetworkRequestCounter networkRequestCounter;

  public MinecraftArtifactDownloader() {
    this(
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build(),
        new MinecraftArtifactVerifier(),
        Duration.ofSeconds(10),
        new MinecraftNetworkRequestCounter());
  }

  public MinecraftArtifactDownloader(
      HttpClient httpClient, MinecraftArtifactVerifier verifier, Duration connectTimeout) {
    this(httpClient, verifier, connectTimeout, new MinecraftNetworkRequestCounter());
  }

  public MinecraftArtifactDownloader(
      HttpClient httpClient,
      MinecraftArtifactVerifier verifier,
      Duration connectTimeout,
      MinecraftNetworkRequestCounter networkRequestCounter) {
    this.httpClient = httpClient;
    this.verifier = verifier;
    this.connectTimeout = connectTimeout;
    this.networkRequestCounter = networkRequestCounter;
  }

  public DownloadResult download(
      URI uri, Path targetPath, Path tmpDirectory, String expectedSha1, Long expectedSize)
      throws LoaderException {
    long start = System.nanoTime();
    Path tempPath =
        tmpDirectory
            .resolve(targetPath.getFileName().toString() + "." + UUID.randomUUID() + ".part")
            .toAbsolutePath()
            .normalize();
    try {
      Files.createDirectories(tmpDirectory);
      Files.createDirectories(targetPath.toAbsolutePath().normalize().getParent());

      HttpRequest request = HttpRequest.newBuilder(uri).GET().timeout(connectTimeout).build();
      networkRequestCounter.incrementAndGet();
      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new LoaderException(
            "Failed to download artifact from " + uri + ": HTTP " + response.statusCode());
      }

      long bytesWritten;
      try (InputStream inputStream = response.body()) {
        bytesWritten = Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
      }
      MinecraftArtifactVerifier.VerificationResult verificationResult =
          verifier.verify(tempPath, expectedSha1, expectedSize);
      Files.move(
          tempPath,
          targetPath,
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
      long durationMs = Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
      return new DownloadResult(
          uri.toString(),
          bytesWritten,
          durationMs,
          true,
          verificationResult.sha256(),
          verificationResult.size(),
          verificationResult.verified());
    } catch (IOException | InterruptedException exception) {
      if (exception instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      deleteQuietly(tempPath);
      throw new LoaderException("Failed to download artifact from " + uri, exception);
    } catch (LoaderException exception) {
      deleteQuietly(tempPath);
      throw exception;
    }
  }

  public int networkRequestCount() {
    return networkRequestCounter.requestCount();
  }

  private void deleteQuietly(Path path) {
    if (path == null) {
      return;
    }
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
    }
  }
}
