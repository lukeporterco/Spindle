package com.spindle.core.minecraft.interpret;

import java.util.ArrayList;
import java.util.List;

final class MinecraftAccessFlags {
  private static final int ACC_PUBLIC = 0x0001;
  private static final int ACC_PRIVATE = 0x0002;
  private static final int ACC_PROTECTED = 0x0004;
  private static final int ACC_STATIC = 0x0008;
  private static final int ACC_FINAL = 0x0010;
  private static final int ACC_SUPER = 0x0020;
  private static final int ACC_SYNCHRONIZED = 0x0020;
  private static final int ACC_VOLATILE = 0x0040;
  private static final int ACC_BRIDGE = 0x0040;
  private static final int ACC_TRANSIENT = 0x0080;
  private static final int ACC_VARARGS = 0x0080;
  private static final int ACC_NATIVE = 0x0100;
  private static final int ACC_INTERFACE = 0x0200;
  private static final int ACC_ABSTRACT = 0x0400;
  private static final int ACC_STRICT = 0x0800;
  private static final int ACC_SYNTHETIC = 0x1000;
  private static final int ACC_ANNOTATION = 0x2000;
  private static final int ACC_ENUM = 0x4000;
  private static final int ACC_MODULE = 0x8000;

  private MinecraftAccessFlags() {}

  static List<String> classFlags(int access) {
    List<String> flags = new ArrayList<>();
    add(flags, access, ACC_PUBLIC, "public");
    add(flags, access, ACC_FINAL, "final");
    add(flags, access, ACC_SUPER, "super");
    add(flags, access, ACC_INTERFACE, "interface");
    add(flags, access, ACC_ABSTRACT, "abstract");
    add(flags, access, ACC_SYNTHETIC, "synthetic");
    add(flags, access, ACC_ANNOTATION, "annotation");
    add(flags, access, ACC_ENUM, "enum");
    add(flags, access, ACC_MODULE, "module");
    return List.copyOf(flags);
  }

  static List<String> fieldFlags(int access) {
    List<String> flags = new ArrayList<>();
    add(flags, access, ACC_PUBLIC, "public");
    add(flags, access, ACC_PRIVATE, "private");
    add(flags, access, ACC_PROTECTED, "protected");
    add(flags, access, ACC_STATIC, "static");
    add(flags, access, ACC_FINAL, "final");
    add(flags, access, ACC_VOLATILE, "volatile");
    add(flags, access, ACC_TRANSIENT, "transient");
    add(flags, access, ACC_SYNTHETIC, "synthetic");
    add(flags, access, ACC_ENUM, "enum");
    return List.copyOf(flags);
  }

  static List<String> methodFlags(int access) {
    List<String> flags = new ArrayList<>();
    add(flags, access, ACC_PUBLIC, "public");
    add(flags, access, ACC_PRIVATE, "private");
    add(flags, access, ACC_PROTECTED, "protected");
    add(flags, access, ACC_STATIC, "static");
    add(flags, access, ACC_FINAL, "final");
    add(flags, access, ACC_SYNCHRONIZED, "synchronized");
    add(flags, access, ACC_BRIDGE, "bridge");
    add(flags, access, ACC_VARARGS, "varargs");
    add(flags, access, ACC_NATIVE, "native");
    add(flags, access, ACC_ABSTRACT, "abstract");
    add(flags, access, ACC_STRICT, "strict");
    add(flags, access, ACC_SYNTHETIC, "synthetic");
    return List.copyOf(flags);
  }

  static boolean isStatic(int access) {
    return (access & ACC_STATIC) != 0;
  }

  private static void add(List<String> flags, int access, int flag, String name) {
    if ((access & flag) != 0) {
      flags.add(name);
    }
  }
}
