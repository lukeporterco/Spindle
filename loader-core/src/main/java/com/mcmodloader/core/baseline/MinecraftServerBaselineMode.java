package com.mcmodloader.core.baseline;

public enum MinecraftServerBaselineMode {
    ACQUIRE("acquire"),
    OFFLINE_REPLAY("offline-replay");

    private final String id;

    MinecraftServerBaselineMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
