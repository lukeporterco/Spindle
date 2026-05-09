package com.spindle.api.config;

import java.util.Set;

public interface ModConfig {
  boolean getBoolean(String key);

  int getInteger(String key);

  double getNumber(String key);

  String getString(String key);

  boolean has(String key);

  Set<String> keys();

  boolean writable();

  void setBoolean(String key, boolean value);

  void setInteger(String key, int value);

  void setNumber(String key, double value);

  void setString(String key, String value);

  static ModConfig empty() {
    return EmptyModConfig.INSTANCE;
  }
}
