package kr.toxicity.hud.api.plugin;

import kr.toxicity.command.BetterCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Info required a reload task.
 * @param sender sender
 * @param flags reload flags
 */
public record ReloadInfo(@NotNull BetterCommandSource sender, @NotNull Set<ReloadFlagType> flags) {
    /**
     * Checks info has this flag.
     * @param type target type
     * @return whether to have or not
     */
    public boolean has(@NotNull ReloadFlagType type) {
        return flags.contains(type);
    }
}
