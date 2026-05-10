package com.spindle.api.config;

import com.spindle.api.exception.ConfigAccessException;
import java.util.Set;

enum EmptyModConfig implements ModConfig {
  INSTANCE;

  @Override
  public boolean getBoolean(String key) {
    throw missingConfig(key);
  }

  @Override
  public int getInteger(String key) {
    throw missingConfig(key);
  }

  @Override
  public double getNumber(String key) {
    throw missingConfig(key);
  }

  @Override
  public String getString(String key) {
    throw missingConfig(key);
  }

  @Override
  public boolean has(String key) {
    return false;
  }

  @Override
  public Set<String> keys() {
    return Set.of();
  }

  @Override
  public boolean writable() {
    return false;
  }

  @Override
  public void setBoolean(String key, boolean value) {
    throw missingConfig(key);
  }

  @Override
  public void setInteger(String key, int value) {
    throw missingConfig(key);
  }

  @Override
  public void setNumber(String key, double value) {
    throw missingConfig(key);
  }

  @Override
  public void setString(String key, String value) {
    throw missingConfig(key);
  }

  private ConfigAccessException missingConfig(String key) {
    return new ConfigAccessException(
        "unavailable",
        key,
        "Mod config is unavailable for key `"
            + key
            + "` because this context does not provide a runtime config view.");
  }
}
