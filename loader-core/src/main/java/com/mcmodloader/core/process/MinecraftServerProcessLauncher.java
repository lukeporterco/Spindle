package com.mcmodloader.core.process;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.minecraft.MinecraftServerLaunchCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class MinecraftServerProcessLauncher {
    public MinecraftProcessResult launch(String minecraftVersion, MinecraftProcessConfig config, Function<Path, String> displayPath)
        throws LoaderException {
        return launch(
            minecraftVersion,
            config,
            MinecraftServerLaunchCommand.simpleJar(
                config.javaExecutable(),
                config.serverJar(),
                config.jvmArgs(),
                config.serverArgs(),
                displayPath
            ),
            displayPath
        );
    }

    public MinecraftProcessResult launch(
        String minecraftVersion,
        MinecraftProcessConfig config,
        MinecraftServerLaunchCommand launchCommand,
        Function<Path, String> displayPath
    )
        throws LoaderException {
        long startNanos = System.nanoTime();
        ensureServerDirectory(config);
        List<String> command = launchCommand.command();

        ProcessOutputCapture outputCapture = new ProcessOutputCapture();
        AtomicBoolean readyDetected = new AtomicBoolean(false);
        AtomicBoolean stopRequested = new AtomicBoolean(false);
        long readyDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(config.readyTimeoutSeconds());
        Process process;
        try {
            process = new ProcessBuilder(command).directory(config.serverDirectory().toFile()).start();
        } catch (IOException exception) {
            throw new LoaderException("Failed to start Minecraft server process: " + exception.getMessage(), exception);
        }

        Thread stdoutThread = createReaderThread(process.inputReader(StandardCharsets.UTF_8), outputCapture::appendStdout, readyDetected, readyDeadlineNanos);
        Thread stderrThread = createReaderThread(process.errorReader(StandardCharsets.UTF_8), outputCapture::appendStderr, readyDetected, readyDeadlineNanos);
        stdoutThread.start();
        stderrThread.start();

        boolean timedOut = false;
        Integer exitCode = null;
        try (OutputStreamWriter processInput = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            long timeoutDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(config.timeoutSeconds());
            while (true) {
                if (process.waitFor(100, TimeUnit.MILLISECONDS)) {
                    exitCode = process.exitValue();
                    break;
                }

                if (config.stopAfterReady() && readyDetected.get() && !stopRequested.get()) {
                    processInput.write("stop%n".formatted());
                    processInput.flush();
                    stopRequested.set(true);
                }

                if (System.nanoTime() >= timeoutDeadlineNanos) {
                    timedOut = true;
                    destroyProcess(process);
                    exitCode = process.isAlive() ? null : process.exitValue();
                    break;
                }
            }
        } catch (IOException exception) {
            destroyProcess(process);
            throw new LoaderException("Failed while controlling Minecraft server process", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            destroyProcess(process);
            throw new LoaderException("Interrupted while waiting for Minecraft server process", exception);
        } finally {
            joinQuietly(stdoutThread);
            joinQuietly(stderrThread);
        }

        long durationMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
        return new MinecraftProcessResult(
            1,
            minecraftVersion,
            displayPath.apply(config.serverDirectory()),
            displayPath.apply(config.serverJar()),
            displayPath.apply(config.javaExecutable()),
            config.jvmArgs(),
            launchCommand.serverArgs(),
            launchCommand.commandPreview(),
            true,
            readyDetected.get(),
            stopRequested.get(),
            exitCode,
            timedOut,
            durationMs,
            outputCapture.stdoutTail(),
            outputCapture.stderrTail()
        );
    }

    private void ensureServerDirectory(MinecraftProcessConfig config) throws LoaderException {
        try {
            Files.createDirectories(config.serverDirectory());
            if (config.acceptEulaForTest()) {
                Files.writeString(config.serverDirectory().resolve("eula.txt"), "eula=true" + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to prepare Minecraft server directory " + config.serverDirectory(), exception);
        }
    }

    private Thread createReaderThread(
        BufferedReader reader,
        java.util.function.Consumer<String> sink,
        AtomicBoolean readyDetected,
        long readyDeadlineNanos
    ) {
        return new Thread(
            () -> {
                try (reader) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sink.accept(line);
                        if (!readyDetected.get() && System.nanoTime() <= readyDeadlineNanos && isReadyLine(line)) {
                            readyDetected.set(true);
                        }
                    }
                } catch (IOException ignored) {
                }
            },
            "minecraft-server-output"
        );
    }

    private boolean isReadyLine(String line) {
        return line.contains("Done (") || line.contains("For help, type");
    }

    private void destroyProcess(Process process) {
        if (!process.isAlive()) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS) && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private void joinQuietly(Thread thread) {
        try {
            thread.join(1_000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
