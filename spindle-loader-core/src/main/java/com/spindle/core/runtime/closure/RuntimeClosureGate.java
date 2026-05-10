package com.spindle.core.runtime.closure;

public record RuntimeClosureGate(
    int order,
    String id,
    String phase,
    boolean beforeClassloading,
    String fatalCondition,
    String note) {}
