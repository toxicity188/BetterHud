package kr.toxicity.hud.api.event;

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
    public CustomPopupEvent(@NotNull Player who, @NotNull String name) {
        super(who);
        this.name = name;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getName() {
        return name;
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
