package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.listener.HudListener;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ListenerManager {
    void addListener(@NotNull String name, @NotNull Function<ConfigurationSection, HudListener> listenerFunction);
}
