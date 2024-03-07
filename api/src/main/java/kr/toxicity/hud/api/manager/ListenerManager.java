package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.listener.HudListener;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ListenerManager {
    void addListener(@NotNull String name, @NotNull Function<ConfigurationSection, Function<UpdateEvent, HudListener>> listenerFunction);
}
