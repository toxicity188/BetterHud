package kr.toxicity.hud.api.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin reload start event.
 */
@Getter
public class PluginReloadStartEvent extends Event implements BetterHudEvent {
    public PluginReloadStartEvent() {
        super(true);
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
