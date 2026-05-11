package com.spindle.core.minecraft.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftHookContractCatalogProviderTest {
  private final MinecraftHookContractCatalogProvider provider =
      new MinecraftHookContractCatalogProvider();

  @Test
  void supportedVersionAndServerSideReturnKnownCatalog() {
    MinecraftHookContractCatalog catalog = provider.catalogFor("26.1.2", MinecraftSide.SERVER);

    assertEquals("minecraft-26.1.2-server-known-symbols", catalog.id());
    assertEquals(
        "Target-3 known-symbol hook contracts for Minecraft 26.1.2 server artifacts.",
        catalog.description());
    assertEquals("26.1.2", catalog.minecraftVersion());
    assertEquals(MinecraftSide.SERVER, catalog.side());
    assertEquals(2, catalog.contracts().size());
    assertEquals(
        List.of("minecraft.26_1_2.server.main.class", "minecraft.26_1_2.server.main.entrypoint"),
        catalog.contracts().stream().map(MinecraftHookPointContract::id).toList());

    MinecraftHookPointContract classContract = catalog.contracts().get(0);
    assertEquals(
        "Known Minecraft 26.1.2 dedicated server entrypoint class.", classContract.description());
    assertEquals(MinecraftSide.SERVER, classContract.side());
    assertEquals(MinecraftHookPointKind.CLASS, classContract.kind());
    assertEquals("net/minecraft/server/Main", classContract.ownerInternalName());
    assertNull(classContract.memberName());
    assertNull(classContract.descriptor());
    assertEquals(MinecraftHookRequirement.REQUIRED, classContract.requirement());

    MinecraftHookPointContract methodContract = catalog.contracts().get(1);
    assertEquals(
        "Known Minecraft 26.1.2 dedicated server main entrypoint method.",
        methodContract.description());
    assertEquals(MinecraftSide.SERVER, methodContract.side());
    assertEquals(MinecraftHookPointKind.METHOD, methodContract.kind());
    assertEquals("net/minecraft/server/Main", methodContract.ownerInternalName());
    assertEquals("main", methodContract.memberName());
    assertEquals("([Ljava/lang/String;)V", methodContract.descriptor());
    assertEquals(MinecraftHookRequirement.REQUIRED, methodContract.requirement());
  }

  @Test
  void unsupportedVersionReturnsEmptyCatalog() {
    MinecraftHookContractCatalog catalog = provider.catalogFor("1.21.8", MinecraftSide.SERVER);

    assertEquals("empty", catalog.id());
    assertNull(catalog.minecraftVersion());
    assertNull(catalog.side());
    assertEquals(List.of(), catalog.contracts());
  }

  @Test
  void clientSideReturnsEmptyCatalog() {
    MinecraftHookContractCatalog catalog = provider.catalogFor("26.1.2", MinecraftSide.CLIENT);

    assertEquals("empty", catalog.id());
    assertNull(catalog.minecraftVersion());
    assertNull(catalog.side());
    assertEquals(List.of(), catalog.contracts());
  }
}
