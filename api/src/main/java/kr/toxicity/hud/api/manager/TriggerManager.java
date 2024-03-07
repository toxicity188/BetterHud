package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.trgger.HudTrigger;
import org.jetbrains.annotations.NotNull;

public interface TriggerManager {
    void addTrigger(@NotNull String name, @NotNull HudTrigger<?> trigger);
}
