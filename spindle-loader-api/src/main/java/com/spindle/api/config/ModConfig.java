package com.spindle.api.config;

import java.util.Set;

/**
 * Stable Runtime API-0 config view for flat declared schema-2 config entries.
 *
 * <p>This interface covers keys materialized by Spindle before lifecycle execution. It does not
 * imply dynamic schemas, nested config trees, or unchecked writes. Callers should expect {@link
 * com.spindle.api.exception.ConfigAccessException} for unsupported key access, type mismatches, or
 * denied writes.
 */
public interface ModConfig {
  /** Returns a declared boolean config value. */
  boolean getBoolean(String key);

  /** Returns a declared integer config value. */
  int getInteger(String key);

  /** Returns a declared number config value. */
  double getNumber(String key);

  /** Returns a declared string config value. */
  String getString(String key);

  /** Returns whether the key was declared in the runtime config contract. */
  boolean has(String key);

  /** Returns declared keys in deterministic sorted order when provided by Spindle. */
  Set<String> keys();

  /** Returns whether runtime writes are currently allowed. */
  boolean writable();

  /** Sets a declared boolean config value. */
  void setBoolean(String key, boolean value);

  /** Sets a declared integer config value. */
  void setInteger(String key, int value);

  /** Sets a declared number config value. */
  void setNumber(String key, double value);

  /** Sets a declared string config value. */
  void setString(String key, String value);

  static ModConfig empty() {
    return EmptyModConfig.INSTANCE;
  }
}
