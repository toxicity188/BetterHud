package kr.toxicity.hud.api.listener;

import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents of hud listener.
 */
public interface HudListener {
    /**
     * Gets the index of given player.
     * value must be 0..1.
     * @param player target player
     * @return index range
     */
    double getValue(@NotNull HudPlayer player);
    HudListener ZERO = p -> 0;

    HudListener EMPTY = p -> -1;
}
