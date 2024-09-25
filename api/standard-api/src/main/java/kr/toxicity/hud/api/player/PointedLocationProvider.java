package kr.toxicity.hud.api.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PointedLocationProvider {
    /**
     * Provides location.
     * @see PointedLocation
     * @param player target player.
     * @return location
     */
    @Nullable
    PointedLocation provide(@NotNull HudPlayer player);
}
