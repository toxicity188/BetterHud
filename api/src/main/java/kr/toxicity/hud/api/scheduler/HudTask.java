package kr.toxicity.hud.api.scheduler;

/**
 * Represents wrapped task between Paper and Folia.
 */
public interface HudTask {
    /**
     * Returns whether this task is cancelled.
     * @return whether to cancel.
     */
    boolean isCancelled();

    /**
     * Cancel this task.
     */
    void cancel();
}
