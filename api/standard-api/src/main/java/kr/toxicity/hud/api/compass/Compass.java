package kr.toxicity.hud.api.compass;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents compass.
 */
public interface Compass extends HudObject {
    /**
     * Indicates some player's compass by some location
     * @see kr.toxicity.hud.api.player.PointedLocationProvider
     * @param player target player
     * @return component
     */
    @NotNull
    WidthComponent indicate(@NotNull HudPlayer player);
}
