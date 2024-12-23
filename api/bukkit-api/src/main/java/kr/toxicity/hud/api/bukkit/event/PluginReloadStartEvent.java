package kr.toxicity.hud.api.bukkit.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin reload start event.
 */
@Getter
public class PluginReloadStartEvent extends Event implements BetterHudEvent {
    /**
     * Plugin reload started.
     */
    public PluginReloadStartEvent() {
        super(true);
    }
    /**
     * Gets event handler
     * @return handler
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    /**
     * Gets event handler
     * @return handler
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
