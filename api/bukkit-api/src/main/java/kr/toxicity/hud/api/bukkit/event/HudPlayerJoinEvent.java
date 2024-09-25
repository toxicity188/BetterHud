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
public class HudPlayerJoinEvent extends PlayerEvent implements BetterHudEvent {
    private final @NotNull HudPlayer hudPlayer;
    public HudPlayerJoinEvent(@NotNull HudPlayer hudPlayer) {
        super((Player) hudPlayer.handle());
        this.hudPlayer = hudPlayer;
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
