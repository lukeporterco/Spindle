package com.mcmodloader.core.minecraft;

public enum MinecraftServerRuntimeMode {
    SIMPLE_JAR("simple-jar"),
    BUNDLED_SERVER("bundled-server");

    private final String id;

    MinecraftServerRuntimeMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
