package com.mcmodloader.core.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class DependencyGraphWriter {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public void write(Path outputPath, FrozenModGraph graph) throws LoaderException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);

        JsonArray nodes = new JsonArray();
        graph
            .builtinDependencies()
            .forEach(builtin -> {
                JsonObject node = new JsonObject();
                node.addProperty("id", builtin.id());
                node.addProperty("kind", "builtin");
                node.addProperty("version", builtin.version());
                nodes.add(node);
            });
        graph
            .mods()
            .stream()
            .sorted(Comparator.comparing(FrozenMod::id))
            .forEach(mod -> {
                JsonObject node = new JsonObject();
                node.addProperty("id", mod.id());
                node.addProperty("kind", "mod");
                node.addProperty("version", mod.version());
                node.addProperty("path", mod.path());
                nodes.add(node);
            });
        root.add("nodes", nodes);

        JsonArray edges = new JsonArray();
        graph
            .dependencyEdges()
            .stream()
            .sorted(Comparator.comparing(DependencyEdge::fromId).thenComparing(DependencyEdge::toId))
            .forEach(edge -> {
                JsonObject jsonEdge = new JsonObject();
                jsonEdge.addProperty("from", edge.fromId());
                jsonEdge.addProperty("to", edge.toId());
                jsonEdge.addProperty("kind", "depends");
                jsonEdge.addProperty("requirement", edge.requirement());
                jsonEdge.addProperty("satisfiedBy", edge.satisfiedBy());
                edges.add(jsonEdge);
            });
        root.add("edges", edges);

        JsonArray incompatibilities = new JsonArray();
        graph
            .incompatibilityEdges()
            .stream()
            .sorted(Comparator.comparing(IncompatibilityEdge::fromId).thenComparing(IncompatibilityEdge::toId))
            .forEach(edge -> {
                JsonObject jsonEdge = new JsonObject();
                jsonEdge.addProperty("from", edge.fromId());
                jsonEdge.addProperty("to", edge.toId());
                jsonEdge.addProperty("kind", "breaks");
                jsonEdge.addProperty("requirement", edge.requirement());
                jsonEdge.addProperty("presentVersion", edge.presentVersion());
                incompatibilities.add(jsonEdge);
            });
        root.add("incompatibilities", incompatibilities);

        try {
            Files.createDirectories(outputPath.getParent());
            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to write dependency graph " + outputPath.getFileName(), exception);
        }
    }
}
