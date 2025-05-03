package kr.toxicity.hud.api.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * Represents the state of reloading.
 */
public sealed interface ReloadState {
    /**
     * Reload success.
     * @param time reload time
     * @param resourcePack resource packs path and byte
     */
    record Success(long time, @NotNull @Unmodifiable Map<String, byte[]> resourcePack) implements ReloadState {
    }

    /**
     * Reload failure.
     * @param throwable reason
     */
    record Failure(@NotNull Throwable throwable) implements ReloadState {
    }

    /**
     * A singleton instance of reload.
     */
    OnReload ON_RELOAD = new OnReload();

    /**
     * Still on reload.
     */
    final class OnReload implements ReloadState {
        private OnReload() {
        }
    }
}
