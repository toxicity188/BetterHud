package kr.toxicity.hud.api.player;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PointedLocationProvider {
    /**
     * Provides location.
     * @see PointedLocation
     * @param player target player.
     * @return location
     */
    @NotNull
    Collection<PointedLocation> provide(@NotNull HudPlayer player);
}
