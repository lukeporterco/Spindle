package com.spindle.core.minecraft.concept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MinecraftTargetConceptCatalogTest {
  private final MinecraftTargetConceptCatalog catalog = new MinecraftTargetConceptCatalog();

  @Test
  void catalogContainsExactlyTenConcepts() {
    assertEquals(10, catalog.concepts().size());
  }

  @Test
  void catalogOrderIsStableAndOneBased() {
    assertEquals(
        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        catalog.concepts().stream().map(MinecraftTargetConcept::order).toList());
  }

  @Test
  void catalogIdsAreUnique() {
    List<String> ids = catalog.concepts().stream().map(MinecraftTargetConcept::id).toList();

    assertEquals(ids.size(), Set.copyOf(ids).size());
    assertEquals(
        List.of(
            "minecraft.concept.server_lifecycle",
            "minecraft.concept.command_registration",
            "minecraft.concept.data_resources_reload",
            "minecraft.concept.registry_bootstrap",
            "minecraft.concept.server_tick_scheduling",
            "minecraft.concept.world_chunk_persistence",
            "minecraft.concept.entity_player_gameplay",
            "minecraft.concept.networking_packets",
            "minecraft.concept.attachable_state",
            "minecraft.concept.controlled_access_bridges"),
        ids);
  }

  @Test
  void catalogExposesServerLifecycleFirst() {
    MinecraftTargetConcept concept = catalog.concepts().getFirst();

    assertEquals("minecraft.concept.server_lifecycle", concept.id());
    assertEquals("Server Lifecycle", concept.displayName());
    assertEquals(MinecraftTargetConceptFamily.SERVER_LIFECYCLE, concept.family());
  }

  @Test
  void catalogExposesControlledAccessLast() {
    MinecraftTargetConcept concept = catalog.concepts().getLast();

    assertEquals("minecraft.concept.controlled_access_bridges", concept.id());
    assertEquals("Controlled Access, Bridges, and Low-Level Escape Hatches", concept.displayName());
    assertEquals(MinecraftTargetConceptFamily.CONTROLLED_ACCESS_BRIDGES, concept.family());
  }

  @Test
  void catalogConceptListsAreImmutable() {
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            catalog
                .concepts()
                .add(
                    new MinecraftTargetConcept(
                        "minecraft.concept.extra",
                        11,
                        "Extra",
                        MinecraftTargetConceptFamily.SERVER_LIFECYCLE,
                        MinecraftTargetConceptLayer.TARGET_LAYER,
                        "Extra concept.",
                        List.of("ExtraConcept"),
                        "DOCUMENTED_ONLY")));

    assertThrows(
        UnsupportedOperationException.class,
        () -> catalog.concepts().getFirst().targetConceptNames().add("MutatedName"));
  }

  @Test
  void catalogFindsConceptById() {
    MinecraftTargetConcept concept =
        catalog.findById("minecraft.concept.networking_packets").orElseThrow();

    assertEquals(8, concept.order());
    assertEquals("Networking and Packet Boundaries", concept.displayName());
    assertTrue(catalog.findById("minecraft.concept.missing").isEmpty());
  }

  @Test
  void catalogConceptsHaveNonblankDescriptions() {
    assertTrue(
        catalog.concepts().stream()
            .map(MinecraftTargetConcept::description)
            .allMatch(description -> description != null && !description.isBlank()));
  }

  @Test
  void catalogConceptsHaveAtLeastOneTargetConceptName() {
    assertTrue(
        catalog.concepts().stream()
            .allMatch(
                concept ->
                    !concept.targetConceptNames().isEmpty()
                        && concept.targetConceptNames().stream()
                            .allMatch(name -> name != null && !name.isBlank())));
  }

  @Test
  void catalogConceptIdsAreNonblank() {
    assertFalse(
        catalog.concepts().stream()
            .map(MinecraftTargetConcept::id)
            .anyMatch(id -> id == null || id.isBlank()));
  }
}
