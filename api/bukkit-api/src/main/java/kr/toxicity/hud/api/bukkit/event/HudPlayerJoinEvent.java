package kr.toxicity.hud.api.bukkit.event;

import kr.toxicity.hud.api.player.HudPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * player's user data load event.
 */
@Getter
public class HudPlayerJoinEvent extends PlayerEvent implements HudPlayerEvent {
    private final @NotNull HudPlayer hudPlayer;

    /**
     * Player joined.
     * @param hudPlayer target player
     */
    public HudPlayerJoinEvent(@NotNull HudPlayer hudPlayer) {
        super((Player) hudPlayer.handle());
        this.hudPlayer = hudPlayer;
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
