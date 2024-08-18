package kr.toxicity.hud.api.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Represents reload result.
 * @param state state
 * @param time time
 */
public record ReloadResult(@NotNull ReloadState state, long time) {
}
