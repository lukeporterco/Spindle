package com.spindle.core.minecraft.hook.steelhook;

import java.net.URL;
import java.util.List;

public record SteelHook02RuntimeClasspathUrls(
    List<URL> urls, List<String> normalizedAbsolutePaths) {
  public SteelHook02RuntimeClasspathUrls {
    urls = List.copyOf(urls == null ? List.of() : urls);
    normalizedAbsolutePaths =
        List.copyOf(normalizedAbsolutePaths == null ? List.of() : normalizedAbsolutePaths);
  }

  public URL[] asArray() {
    return urls.toArray(URL[]::new);
  }
}
