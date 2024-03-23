package kr.toxicity.hud.api.event;

import kr.toxicity.hud.api.player.HudPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class HudPlayerJoinEvent extends PlayerEvent implements BetterHudEvent {
    private final @NotNull HudPlayer hudPlayer;
    public HudPlayerJoinEvent(@NotNull Player who, @NotNull HudPlayer hudPlayer) {
        super(who);
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
