package com.mcmodloader.core.graph;

public record DependencyEdge(String fromId, String toId, String requirement, String satisfiedBy) {
}
