package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.adapter.LocationWrapper;
import org.jetbrains.annotations.NotNull;

public record PointedLocation(@NotNull PointedLocationSource source, @NotNull String name, @NotNull LocationWrapper location) {
}
