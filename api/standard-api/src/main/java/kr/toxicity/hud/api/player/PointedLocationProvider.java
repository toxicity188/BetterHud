package kr.toxicity.hud.api.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * A provider of pointer location.
 */
public interface PointedLocationProvider {
    /**
     * Provides location.
     * @see PointedLocation
     * @param player target player.
     * @return location
     */
    @NotNull
    @Unmodifiable
    Collection<PointedLocation> provide(@NotNull HudPlayer player);
}
