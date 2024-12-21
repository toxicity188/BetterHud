package kr.toxicity.hud.api.bukkit.event;

import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Player event
 */
public interface HudPlayerEvent extends BetterHudEvent {
    /**
     * Gets called player
     * @return player
     */
    @NotNull HudPlayer getHudPlayer();
}
