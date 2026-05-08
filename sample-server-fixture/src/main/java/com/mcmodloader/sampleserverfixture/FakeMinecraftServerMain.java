package com.mcmodloader.sampleserverfixture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class FakeMinecraftServerMain {
    private FakeMinecraftServerMain() {
    }

    public static void main(String[] args) throws Exception {
        boolean skipReady = false;
        boolean writeStderr = false;
        int sleepSeconds = 0;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if ("--skip-ready".equals(argument)) {
                skipReady = true;
                continue;
            }
            if ("--write-stderr".equals(argument)) {
                writeStderr = true;
                continue;
            }
            if ("--sleep-seconds".equals(argument) && index + 1 < args.length) {
                sleepSeconds = Integer.parseInt(args[++index]);
            }
        }

        System.out.println("Starting fake Minecraft server");
        if (writeStderr) {
            System.err.println("Fake server stderr line");
        }
        if (!skipReady) {
            System.out.println("Done (0.1s)! For help, type \"help\"");
        }
        if (sleepSeconds > 0) {
            Thread.sleep(sleepSeconds * 1_000L);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ("stop".equals(line.trim())) {
                    System.out.println("Stopping fake Minecraft server");
                    return;
                }
            }
        }
    }
}
