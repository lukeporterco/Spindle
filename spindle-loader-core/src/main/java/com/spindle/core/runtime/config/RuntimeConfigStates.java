package com.spindle.core.runtime.config;

public final class RuntimeConfigStates {
  public static final String MOD_NONE = "none";
  public static final String MOD_VALID = "valid";
  public static final String MOD_DEFAULTED = "defaulted";
  public static final String MOD_INVALID = "invalid";
  public static final String MOD_STORAGE_NOT_GRANTED = "storage-not-granted";

  public static final String ENTRY_VALID = "valid";
  public static final String ENTRY_DEFAULTED = "defaulted";
  public static final String ENTRY_MISSING_DEFAULTED = "missing-defaulted";
  public static final String ENTRY_INVALID_TYPE = "invalid-type";
  public static final String ENTRY_INVALID_RANGE = "invalid-range";
  public static final String ENTRY_INVALID_OPTION = "invalid-option";

  private RuntimeConfigStates() {}
}
