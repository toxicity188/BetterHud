package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.trigger.HudTrigger;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents trigger manager.
 */
public interface TriggerManager {
    /**
     * Adds trigger by builder.
     * @param name trigger name
     * @param trigger trigger builder
     */
    void addTrigger(@NotNull String name, @NotNull Function<ConfigurationSection, HudTrigger<?>> trigger);
}
