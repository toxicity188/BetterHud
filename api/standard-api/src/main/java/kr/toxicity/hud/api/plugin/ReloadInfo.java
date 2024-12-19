package kr.toxicity.hud.api.plugin;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ReloadInfo(@NotNull Audience sender, @NotNull Set<ReloadFlagType> flags) {
    public boolean has(@NotNull ReloadFlagType type) {
        return flags.contains(type);
    }
}
