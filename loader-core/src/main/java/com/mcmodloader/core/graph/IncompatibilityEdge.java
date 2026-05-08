package com.mcmodloader.core.graph;

public record IncompatibilityEdge(String fromId, String toId, String requirement, String presentVersion) {
}
