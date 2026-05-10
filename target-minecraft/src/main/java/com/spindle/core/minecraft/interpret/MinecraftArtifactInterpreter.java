package com.spindle.core.minecraft.interpret;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftSide;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class MinecraftArtifactInterpreter {
  private static final String MILESTONE_NAME = "Target-1";
  private final MinecraftClassFileReader classFileReader = new MinecraftClassFileReader();

  public MinecraftArtifactInterpretation interpret(
      String minecraftVersion, MinecraftSide side, List<JarInput> jarInputs)
      throws LoaderException {
    List<JarInput> sortedInputs =
        jarInputs.stream().sorted(Comparator.comparing(JarInput::displayPath)).toList();
    List<MinecraftInterpretedJar> jars = new ArrayList<>();
    TreeSet<String> packages = new TreeSet<>();
    int classCount = 0;
    int fieldCount = 0;
    int methodCount = 0;
    int constructorCount = 0;

    for (JarInput input : sortedInputs) {
      MinecraftInterpretedJar jar = interpretJar(input);
      jars.add(jar);
      packages.addAll(jar.packages());
      classCount += jar.classCount();
      fieldCount += jar.fieldCount();
      methodCount += jar.methodCount();
      constructorCount += jar.constructorCount();
    }

    return new MinecraftArtifactInterpretation(
        1,
        MILESTONE_NAME,
        "minecraft",
        minecraftVersion,
        side.id(),
        true,
        false,
        false,
        false,
        false,
        false,
        "dry-run-analysis",
        List.copyOf(jars),
        packages.size(),
        classCount,
        fieldCount,
        methodCount,
        constructorCount,
        List.copyOf(packages),
        List.of());
  }

  private MinecraftInterpretedJar interpretJar(JarInput input) throws LoaderException {
    List<MinecraftInterpretedClass> classes = new ArrayList<>();
    TreeSet<String> packages = new TreeSet<>();

    try (JarFile jarFile = new JarFile(input.path().toFile())) {
      List<? extends ZipEntry> entries =
          jarFile.stream()
              .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
              .sorted(Comparator.comparing(ZipEntry::getName))
              .toList();
      for (ZipEntry entry : entries) {
        byte[] classBytes;
        try (var inputStream = jarFile.getInputStream(entry)) {
          classBytes = inputStream.readAllBytes();
        }
        MinecraftInterpretedClass interpretedClass =
            classFileReader.read(classBytes, input.path().toString(), entry.getName());
        classes.add(sortMembers(interpretedClass));
        if (!interpretedClass.packageName().isBlank()) {
          packages.add(interpretedClass.packageName());
        }
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to interpret Minecraft artifact jar " + input.path(), exception);
    }

    List<MinecraftInterpretedClass> sortedClasses =
        classes.stream()
            .sorted(Comparator.comparing(MinecraftInterpretedClass::binaryName))
            .toList();
    int fieldCount = sortedClasses.stream().mapToInt(value -> value.fields().size()).sum();
    int methodCount = sortedClasses.stream().mapToInt(value -> value.methods().size()).sum();
    int constructorCount =
        sortedClasses.stream()
            .mapToInt(
                value ->
                    (int)
                        value.methods().stream()
                            .filter(MinecraftInterpretedMethod::constructor)
                            .count())
            .sum();
    return new MinecraftInterpretedJar(
        input.displayPath(),
        input.ownership(),
        input.origin(),
        input.sha256(),
        sortedClasses.size(),
        fieldCount,
        methodCount,
        constructorCount,
        List.copyOf(packages),
        sortedClasses);
  }

  private MinecraftInterpretedClass sortMembers(MinecraftInterpretedClass interpretedClass) {
    List<MinecraftInterpretedField> fields =
        interpretedClass.fields().stream()
            .sorted(
                Comparator.comparing(MinecraftInterpretedField::name)
                    .thenComparing(MinecraftInterpretedField::descriptor))
            .toList();
    List<MinecraftInterpretedMethod> methods =
        interpretedClass.methods().stream()
            .sorted(
                Comparator.comparing(MinecraftInterpretedMethod::name)
                    .thenComparing(MinecraftInterpretedMethod::descriptor))
            .toList();
    return new MinecraftInterpretedClass(
        interpretedClass.binaryName(),
        interpretedClass.internalName(),
        interpretedClass.packageName(),
        interpretedClass.superName(),
        interpretedClass.interfaces(),
        interpretedClass.access(),
        interpretedClass.accessFlags(),
        fields,
        methods);
  }

  public record JarInput(
      Path path, String displayPath, String ownership, String origin, String sha256) {
    public JarInput {
      path = path.toAbsolutePath().normalize();
      displayPath = displayPath == null ? path.toString().replace('\\', '/') : displayPath;
    }

    public static JarInput of(
        Path path, String displayPath, String ownership, String origin, String sha256) {
      return new JarInput(path, displayPath, ownership, origin, sha256);
    }
  }
}
