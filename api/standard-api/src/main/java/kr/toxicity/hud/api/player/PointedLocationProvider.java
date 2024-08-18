package kr.toxicity.hud.api.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PointedLocationProvider {
    @Nullable
    PointedLocation provide(@NotNull HudPlayer player);
}
