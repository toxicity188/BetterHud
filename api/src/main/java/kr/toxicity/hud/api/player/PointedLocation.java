package kr.toxicity.hud.api.player;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public record PointedLocation(@NotNull PointedLocationSource source, @NotNull String name, @NotNull Location location) {
}
