package com.spindle.core.graph;

public record IncompatibilityEdge(
    String fromId, String toId, String requirement, String presentVersion) {}
