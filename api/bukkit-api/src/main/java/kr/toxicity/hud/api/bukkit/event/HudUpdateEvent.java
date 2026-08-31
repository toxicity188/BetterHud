package kr.toxicity.hud.api.bukkit.event;

import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * player update event.
 */
public class HudUpdateEvent extends PlayerEvent implements HudPlayerEvent {

    private final @NotNull HudPlayer player;

    /**
     * Player's hud is updated.
     * @param player player
     */
    public HudUpdateEvent(@NotNull HudPlayer player) {
        super((Player) player.handle());
        this.player = player;
    }

    @Override
    public @NotNull HudPlayer player() {
        return player;
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
