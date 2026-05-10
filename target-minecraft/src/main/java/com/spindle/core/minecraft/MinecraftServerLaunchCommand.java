package com.spindle.core.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record MinecraftServerLaunchCommand(
    Path javaExecutable,
    List<String> jvmArgs,
    MinecraftServerRuntimeMode mode,
    String mainClass,
    List<Path> classpath,
    Path serverJar,
    List<String> serverArgs,
    List<String> command,
    List<String> commandPreview) {
  public MinecraftServerLaunchCommand {
    jvmArgs = List.copyOf(jvmArgs == null ? List.of() : jvmArgs);
    classpath = List.copyOf(classpath == null ? List.of() : classpath);
    serverArgs =
        List.copyOf(serverArgs == null || serverArgs.isEmpty() ? List.of("nogui") : serverArgs);
    command = List.copyOf(command);
    commandPreview = List.copyOf(commandPreview);
  }

  public static MinecraftServerLaunchCommand simpleJar(
      Path javaExecutable,
      Path serverJar,
      List<String> jvmArgs,
      List<String> serverArgs,
      Function<Path, String> displayPath) {
    List<String> effectiveServerArgs =
        serverArgs == null || serverArgs.isEmpty() ? List.of("nogui") : List.copyOf(serverArgs);
    List<String> command = new ArrayList<>();
    command.add(javaExecutable.toString());
    command.addAll(jvmArgs == null ? List.of() : jvmArgs);
    command.add("-jar");
    command.add(serverJar.toString());
    command.addAll(effectiveServerArgs);

    List<String> preview = new ArrayList<>();
    preview.add("java");
    preview.addAll(jvmArgs == null ? List.of() : jvmArgs);
    preview.add("-jar");
    preview.add(displayPath.apply(serverJar));
    preview.addAll(effectiveServerArgs);

    return new MinecraftServerLaunchCommand(
        javaExecutable,
        jvmArgs,
        MinecraftServerRuntimeMode.SIMPLE_JAR,
        null,
        List.of(),
        serverJar,
        effectiveServerArgs,
        command,
        preview);
  }

  public static MinecraftServerLaunchCommand classpath(
      Path javaExecutable,
      String mainClass,
      List<Path> classpath,
      Path serverJar,
      List<String> jvmArgs,
      List<String> serverArgs,
      Function<Path, String> displayPath) {
    List<String> effectiveServerArgs =
        serverArgs == null || serverArgs.isEmpty() ? List.of("nogui") : List.copyOf(serverArgs);
    String separator = System.getProperty("path.separator");
    String rawClasspath = String.join(separator, classpath.stream().map(Path::toString).toList());
    String previewClasspath = String.join(separator, classpath.stream().map(displayPath).toList());

    List<String> command = new ArrayList<>();
    command.add(javaExecutable.toString());
    command.addAll(jvmArgs == null ? List.of() : jvmArgs);
    command.add("-cp");
    command.add(rawClasspath);
    command.add(mainClass);
    command.addAll(effectiveServerArgs);

    List<String> preview = new ArrayList<>();
    preview.add("java");
    preview.addAll(jvmArgs == null ? List.of() : jvmArgs);
    preview.add("-cp");
    preview.add(previewClasspath);
    preview.add(mainClass);
    preview.addAll(effectiveServerArgs);

    return new MinecraftServerLaunchCommand(
        javaExecutable,
        jvmArgs,
        MinecraftServerRuntimeMode.BUNDLED_SERVER,
        mainClass,
        classpath,
        serverJar,
        effectiveServerArgs,
        command,
        preview);
  }
}
