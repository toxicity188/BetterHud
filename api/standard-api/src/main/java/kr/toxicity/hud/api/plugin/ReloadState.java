package kr.toxicity.hud.api.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * Represents the state of reloading.
 */
public sealed interface ReloadState {
    record Success(long time, @NotNull @Unmodifiable Map<String, byte[]> resourcePack) implements ReloadState {
    }
    record Failure(@NotNull Throwable throwable) implements ReloadState {
    }

    OnReload ON_RELOAD = new OnReload();

    final class OnReload implements ReloadState {
        private OnReload() {
        }
    }
}
