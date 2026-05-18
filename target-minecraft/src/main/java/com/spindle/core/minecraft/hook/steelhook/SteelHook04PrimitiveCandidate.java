package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04PrimitiveCandidate(
    String id,
    SteelHook04PrimitiveKind primitiveKind,
    SteelHook04PrimitiveCandidateStatus candidateStatus,
    boolean internalOnly,
    boolean publicApiExposed,
    boolean nonPublicApi,
    boolean runtimeReady,
    boolean gatedRuntimeReady,
    boolean implementedInTarget32,
    String targetFollowOnPass,
    String fixtureShapeSummary,
    String evidenceSummary,
    List<SteelHook04FixtureShape> allowedFixtureShapes,
    List<String> notes) {
  public SteelHook04PrimitiveCandidate {
    allowedFixtureShapes =
        List.copyOf(allowedFixtureShapes == null ? List.of() : allowedFixtureShapes);
    notes = List.copyOf(notes == null ? List.of() : notes);
  }
}
