package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.listener.HudListener;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents listener manager.
 */
public interface ListenerManager {
    /**
     * Adds listener builder.
     * @param name listener name
     * @param listenerFunction builder
     */
    void addListener(@NotNull String name, @NotNull Function<ConfigurationSection, Function<UpdateEvent, HudListener>> listenerFunction);
}
