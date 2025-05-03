package kr.toxicity.hud.api.scheduler;

/**
 * Represents a wrapped task between Paper and Folia.
 */
public interface HudTask {
    /**
     * Returns whether this task is canceled.
     * @return whether to cancel.
     */
    boolean isCancelled();

    /**
     * Cancel this task.
     */
    void cancel();
}
