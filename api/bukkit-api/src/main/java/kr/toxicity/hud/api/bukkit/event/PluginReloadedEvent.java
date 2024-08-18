package kr.toxicity.hud.api.bukkit.event;

import kr.toxicity.hud.api.plugin.ReloadResult;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin reloaded event.
 */
@Getter
public class PluginReloadedEvent extends Event implements BetterHudEvent {
    private final ReloadResult result;
    public PluginReloadedEvent(@NotNull ReloadResult result) {
        super(true);
        this.result = result;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
