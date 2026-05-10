package com.spindle.core.minecraft.hook;

import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class MinecraftHookContractIndexes {
  private final Map<String, MinecraftInterpretedClass> classesByInternalName;
  private final Set<String> methods;
  private final Set<String> constructors;
  private final Set<String> fields;

  private MinecraftHookContractIndexes(
      Map<String, MinecraftInterpretedClass> classesByInternalName,
      Set<String> methods,
      Set<String> constructors,
      Set<String> fields) {
    this.classesByInternalName = Map.copyOf(classesByInternalName);
    this.methods = Set.copyOf(methods);
    this.constructors = Set.copyOf(constructors);
    this.fields = Set.copyOf(fields);
  }

  static MinecraftHookContractIndexes from(MinecraftArtifactInterpretation interpretation) {
    Map<String, MinecraftInterpretedClass> classesByInternalName = new TreeMap<>();
    Set<String> methods = new TreeSet<>();
    Set<String> constructors = new TreeSet<>();
    Set<String> fields = new TreeSet<>();
    for (MinecraftInterpretedJar jar : interpretation.jars()) {
      for (MinecraftInterpretedClass interpretedClass : jar.classes()) {
        classesByInternalName.put(interpretedClass.internalName(), interpretedClass);
        for (MinecraftInterpretedMethod method : interpretedClass.methods()) {
          String key = key(interpretedClass.internalName(), method.name(), method.descriptor());
          if (method.constructor()) {
            constructors.add(key);
          } else {
            methods.add(key);
          }
        }
        for (MinecraftInterpretedField field : interpretedClass.fields()) {
          fields.add(key(interpretedClass.internalName(), field.name(), field.descriptor()));
        }
      }
    }
    return new MinecraftHookContractIndexes(classesByInternalName, methods, constructors, fields);
  }

  boolean hasClass(String ownerInternalName) {
    return classesByInternalName.containsKey(ownerInternalName);
  }

  boolean hasMethod(String ownerInternalName, String memberName, String descriptor) {
    return methods.contains(key(ownerInternalName, memberName, descriptor));
  }

  boolean hasConstructor(String ownerInternalName, String descriptor) {
    return constructors.contains(key(ownerInternalName, "<init>", descriptor));
  }

  boolean hasField(String ownerInternalName, String memberName, String descriptor) {
    return fields.contains(key(ownerInternalName, memberName, descriptor));
  }

  private static String key(String ownerInternalName, String memberName, String descriptor) {
    return ownerInternalName + '\u0000' + memberName + '\u0000' + descriptor;
  }
}
