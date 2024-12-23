package kr.toxicity.hud.api.bukkit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom popup event.
 */
@Getter
public class CustomPopupEvent extends PlayerEvent implements BetterHudEvent {
    private final String name;
    private final Map<String, String> variables = new HashMap<>();

    /**
     * Custom popup with name
     * @param who player
     * @param name identifier
     */
    public CustomPopupEvent(@NotNull Player who, @NotNull String name) {
        super(who);
        this.name = name;
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
