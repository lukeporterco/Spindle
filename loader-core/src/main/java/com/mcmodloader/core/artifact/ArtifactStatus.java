package com.mcmodloader.core.artifact;

public enum ArtifactStatus {
    VERIFIED("verified"),
    PRESENT("present"),
    MISSING("missing"),
    INVALID("invalid"),
    UNVERIFIABLE("unverifiable");

    private final String id;

    ArtifactStatus(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
