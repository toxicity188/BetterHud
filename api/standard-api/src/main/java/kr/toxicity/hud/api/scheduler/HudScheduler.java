package kr.toxicity.hud.api.scheduler;

import kr.toxicity.hud.api.adapter.LocationWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the adapted scheduler between Paper and Folia.
 */
public interface HudScheduler {
    /**
     * Executes sync task.
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask task(@NotNull Runnable runnable);
    /**
     * Executes sync task.
     * @param location location
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask task(@NotNull LocationWrapper location, @NotNull Runnable runnable);
    /**
     * Executes sync task.
     * @param delay delay
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask taskLater(long delay, @NotNull Runnable runnable);
    /**
     * Executes async task.
     * @param runnable task
     * @return scheduled task.
     */
    @NotNull HudTask asyncTask(@NotNull Runnable runnable);
    /**
     * Executes async task.
     * @param runnable task
     * @param delay delay
     * @param period period
     * @return scheduled task.
     */
    @NotNull HudTask asyncTaskTimer(long delay, long period, @NotNull Runnable runnable);
}
