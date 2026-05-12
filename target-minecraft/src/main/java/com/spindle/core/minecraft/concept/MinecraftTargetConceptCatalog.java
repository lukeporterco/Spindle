package com.spindle.core.minecraft.concept;

import java.util.List;
import java.util.Optional;

public final class MinecraftTargetConceptCatalog {
  private static final List<MinecraftTargetConcept> CONCEPTS =
      List.of(
          new MinecraftTargetConcept(
              "minecraft.concept.server_lifecycle",
              1,
              "Server Lifecycle",
              MinecraftTargetConceptFamily.SERVER_LIFECYCLE,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for server startup, ready, stop, and failure boundaries above internal SteelHook mechanics.",
              List.of(
                  "MinecraftServerLifecycle",
                  "MinecraftServerStartContext",
                  "MinecraftServerStopReason"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.command_registration",
              2,
              "Command Registration",
              MinecraftTargetConceptFamily.COMMAND_REGISTRATION,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for future Minecraft command registration boundaries and registration-time context.",
              List.of(
                  "MinecraftCommandRegistrar",
                  "MinecraftCommandRegistrationContext",
                  "MinecraftCommandTreeAccess"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.data_resources_reload",
              3,
              "Data, Resources, Reload, and Future Data Generation",
              MinecraftTargetConceptFamily.DATA_RESOURCES_RELOAD,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for resource reload, server data visibility, and deferred data-generation-adjacent planning.",
              List.of(
                  "MinecraftResourceReloadPhase",
                  "MinecraftDataPackView",
                  "MinecraftReloadContext"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.registry_bootstrap",
              4,
              "Registry Bootstrap and Content Registration",
              MinecraftTargetConceptFamily.REGISTRY_BOOTSTRAP,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for future registry bootstrap and content registration boundaries.",
              List.of(
                  "MinecraftRegistryBootstrap",
                  "MinecraftContentRegistrationContext",
                  "MinecraftRegistryKey"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.server_tick_scheduling",
              5,
              "Server Tick and Scheduled Work",
              MinecraftTargetConceptFamily.SERVER_TICK_SCHEDULING,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for tick-phase ownership and loader-approved scheduled server work.",
              List.of("MinecraftServerTickPhase", "MinecraftScheduledWork", "MinecraftTickContext"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.world_chunk_persistence",
              6,
              "World, Dimension, Chunk, and Persistence Lifecycle",
              MinecraftTargetConceptFamily.WORLD_CHUNK_PERSISTENCE,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for world lifecycle, dimension access, chunk persistence, and save boundaries.",
              List.of(
                  "MinecraftWorldLifecycle",
                  "MinecraftDimensionHandle",
                  "MinecraftChunkPersistenceContext"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.entity_player_gameplay",
              7,
              "Entity, Player, Interaction, and Gameplay Boundaries",
              MinecraftTargetConceptFamily.ENTITY_PLAYER_GAMEPLAY,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for future entity, player, interaction, and gameplay-facing target boundaries.",
              List.of(
                  "MinecraftEntityAccess",
                  "MinecraftPlayerContext",
                  "MinecraftInteractionBoundary"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.networking_packets",
              8,
              "Networking and Packet Boundaries",
              MinecraftTargetConceptFamily.NETWORKING_PACKETS,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for future packet boundaries, connection roles, and controlled message handling.",
              List.of(
                  "MinecraftPacketBoundary",
                  "MinecraftConnectionRole",
                  "MinecraftNetworkingContext"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.attachable_state",
              9,
              "Attachable State, Components, and Capability-Style Extension",
              MinecraftTargetConceptFamily.ATTACHABLE_STATE,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for target-owned attachment, component, and extension boundaries.",
              List.of(
                  "MinecraftAttachableState",
                  "MinecraftComponentKey",
                  "MinecraftAttachmentContext"),
              "DOCUMENTED_ONLY"),
          new MinecraftTargetConcept(
              "minecraft.concept.controlled_access_bridges",
              10,
              "Controlled Access, Bridges, and Low-Level Escape Hatches",
              MinecraftTargetConceptFamily.CONTROLLED_ACCESS_BRIDGES,
              MinecraftTargetConceptLayer.MIXED,
              "Documentation/model-only concept family for bounded low-level target bridges without exposing raw SteelHook internals.",
              List.of(
                  "MinecraftControlledAccess",
                  "MinecraftTargetBridge",
                  "MinecraftLowLevelOperation"),
              "DOCUMENTED_ONLY"));

  public List<MinecraftTargetConcept> concepts() {
    return CONCEPTS;
  }

  public Optional<MinecraftTargetConcept> findById(String id) {
    return CONCEPTS.stream().filter(concept -> concept.id().equals(id)).findFirst();
  }
}
