package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.trgger.HudTrigger;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface TriggerManager {
    void addTrigger(@NotNull String name, @NotNull Function<ConfigurationSection, HudTrigger<?>> trigger);
}
