package kr.toxicity.hud.api.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the adapted scheduler between Paper and Folia.
 */
public interface HudScheduler {
    /**
     * Executes sync task.
     * @param plugin target plugin.
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask task(@NotNull Plugin plugin, @NotNull Runnable runnable);
    /**
     * Executes sync task.
     * @param plugin target plugin.
     * @param delay delay
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask taskLater(@NotNull Plugin plugin, long delay, @NotNull Runnable runnable);
    /**
     * Executes async task.
     * @param plugin target plugin.
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask asyncTask(@NotNull Plugin plugin, @NotNull Runnable runnable);
    /**
     * Executes async task.
     * @param plugin target plugin.
     * @param runnable task
     * @param delay delay
     * @param period period
     * @return scheduled task.
     */
    @NotNull HudTask asyncTaskTimer(@NotNull Plugin plugin, long delay, long period, @NotNull Runnable runnable);
}
