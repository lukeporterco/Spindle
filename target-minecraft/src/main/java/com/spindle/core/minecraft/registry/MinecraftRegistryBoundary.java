package com.spindle.core.minecraft.registry;

public enum MinecraftRegistryBoundary {
  RESOURCE_RELOAD_ARC_HANDOFF(
      "minecraft.registries.resource_reload_arc_handoff", "Resource/reload arc handoff"),
  REGISTRY_SYMBOL_DISCOVERY("minecraft.registries.symbol.discovery", "Registry symbol discovery"),
  REGISTRY_BOOTSTRAP_WINDOW("minecraft.registries.bootstrap_window", "Registry bootstrap window"),
  ROOT_REGISTRY_ACCESS("minecraft.registries.root_registry_access", "Root registry access"),
  REGISTRY_KEY_MODEL("minecraft.registries.key_model", "Registry key model"),
  CONTENT_REGISTRATION_WINDOW(
      "minecraft.registries.content_registration_window", "Content registration window"),
  CONTENT_REGISTRATION_APPLY(
      "minecraft.registries.content_registration_apply", "Content registration apply"),
  DYNAMIC_REGISTRY_RELOAD_LINK(
      "minecraft.registries.dynamic_registry_reload_link", "Dynamic registry/reload link");

  private final String id;
  private final String displayName;

  MinecraftRegistryBoundary(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
  }

  public String id() {
    return id;
  }

  public String displayName() {
    return displayName;
  }
}
