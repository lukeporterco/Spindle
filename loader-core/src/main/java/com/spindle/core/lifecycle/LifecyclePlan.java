package com.spindle.core.lifecycle;

import java.util.List;

public record LifecyclePlan(List<String> phaseOrder, List<LifecycleHandlerDeclaration> handlers) {
  public LifecyclePlan {
    phaseOrder = List.copyOf(phaseOrder);
    handlers = List.copyOf(handlers);
  }
}
