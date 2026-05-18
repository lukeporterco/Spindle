package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04PrimitiveBoundaryStatus {
  BOUNDARY_READY("boundary-ready"),
  SOURCE_GATE_BLOCKED("source-gate-blocked");

  private final String id;

  SteelHook04PrimitiveBoundaryStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
