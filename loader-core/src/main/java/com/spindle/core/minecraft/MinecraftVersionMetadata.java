package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;

public record MinecraftVersionMetadata(
    String id,
    String type,
    String mainClass,
    String assets,
    AssetIndex assetIndex,
    Download clientDownload,
    Download serverDownload,
    List<Library> libraries,
    Arguments arguments,
    String legacyMinecraftArguments) {
  public MinecraftVersionMetadata {
    libraries = List.copyOf(libraries);
    arguments = arguments == null ? new Arguments(List.of(), List.of()) : arguments;
  }

  public record AssetIndex(String id, String url, String sha1, long size) {}

  public record Download(String path, String url, String sha1, long size) {}

  public record Library(
      String name,
      Download artifact,
      Map<String, Download> classifiers,
      List<Rule> rules,
      Map<String, String> natives) {
    public Library {
      classifiers = Map.copyOf(classifiers);
      rules = List.copyOf(rules);
      natives = Map.copyOf(natives);
    }
  }

  public record Rule(String action, String osName, String osArch, String osVersion) {}

  public record Arguments(List<Argument> game, List<Argument> jvm) {
    public Arguments {
      game = List.copyOf(game);
      jvm = List.copyOf(jvm);
    }
  }

  public record Argument(List<Rule> rules, List<String> values) {
    public Argument {
      rules = List.copyOf(rules);
      values = List.copyOf(values);
    }
  }
}
