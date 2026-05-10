package com.spindle.api;

/**
 * Stable compatibility entrypoint for schema-1 style mod initialization.
 *
 * <p>Runtime API-0 prefers schema-2 lifecycle handlers that receive {@link ModContext}, but this
 * interface remains a stable compatibility surface for older mod metadata.
 */
public interface ModInitializer {
  void onInitialize();
}
