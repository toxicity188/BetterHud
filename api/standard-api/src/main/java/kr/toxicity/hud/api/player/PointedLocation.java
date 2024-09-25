package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.adapter.LocationWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Location with source and name.
 * @param source source
 * @param name name
 * @param location location
 */
public record PointedLocation(@NotNull PointedLocationSource source, @NotNull String name, @NotNull LocationWrapper location) {
}
