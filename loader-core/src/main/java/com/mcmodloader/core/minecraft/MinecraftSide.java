package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;

public enum MinecraftSide {
    CLIENT("client"),
    SERVER("server");

    private final String id;

    MinecraftSide(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static MinecraftSide fromCliValue(String value) throws LoaderException {
        if ("client".equalsIgnoreCase(value)) {
            return CLIENT;
        }
        if ("server".equalsIgnoreCase(value)) {
            return SERVER;
        }
        throw new LoaderException("Unsupported value for --minecraft-side: " + value);
    }
}
