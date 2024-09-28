package kr.toxicity.hud.api.player;

import kr.toxicity.hud.api.adapter.LocationWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Location with source and name.
 * @param source source
 * @param name name
 * @param icon pointer name
 * @param location location
 */
public record PointedLocation(
        @NotNull PointedLocationSource source,
        @NotNull String name,
        @Nullable String icon,
        @NotNull LocationWrapper location
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointedLocation that = (PointedLocation) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
