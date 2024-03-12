package kr.toxicity.hud.api.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface HudScheduler {
    @NotNull HudTask task(@NotNull Plugin plugin, @NotNull Runnable runnable);
    @NotNull HudTask asyncTask(@NotNull Plugin plugin, @NotNull Runnable runnable);
    @NotNull HudTask asyncTaskTimer(@NotNull Plugin plugin, long delay, long period, @NotNull Runnable runnable);
}
