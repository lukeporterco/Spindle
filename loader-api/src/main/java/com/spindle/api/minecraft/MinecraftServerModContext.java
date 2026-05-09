package com.spindle.api.minecraft;

import java.nio.file.Path;

public interface MinecraftServerModContext {
  String modId();

  String modVersion();

  String minecraftVersion();

  String loaderVersion();

  String side();

  Path gameDirectory();

  Path configDirectory();

  Path modDataDirectory();
}
