package com.spindle.core.graph;

public record DependencyEdge(String fromId, String toId, String requirement, String satisfiedBy) {}
