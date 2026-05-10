package com.spindle.core.runtime.service;

final class RuntimeServiceStates {
  static final String AVAILABLE = "available";
  static final String CONFLICT = "conflict";
  static final String IMPLEMENTATION_MISSING = "implementation-missing";
  static final String IMPLEMENTATION_NOT_OWNED = "implementation-not-owned";
  static final String BOUND = "bound";
  static final String OPTIONAL_UNBOUND = "optional-unbound";
  static final String REQUIRED_UNBOUND = "required-unbound";
  static final String PROVIDER_CONFLICT = "provider-conflict";
  static final String TYPE_MISMATCH = "type-mismatch";

  private RuntimeServiceStates() {}
}
