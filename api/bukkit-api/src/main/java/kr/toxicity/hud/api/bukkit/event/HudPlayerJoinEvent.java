package kr.toxicity.hud.api.bukkit.event;

import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * player's user data load event.
 */
public class HudPlayerJoinEvent extends PlayerEvent implements HudPlayerEvent {

    private final @NotNull HudPlayer player;

    /**
     * Player joined.
     * @param player target player
     */
    public HudPlayerJoinEvent(@NotNull HudPlayer player) {
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
