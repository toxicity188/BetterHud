package kr.toxicity.hud.api.scheduler;

public interface HudTask {
    boolean isCancelled();
    void cancel();
}
