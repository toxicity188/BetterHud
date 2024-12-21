package kr.toxicity.hud.api.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.toxicity.hud.api.adapter.LocationWrapper;
import kr.toxicity.hud.api.yaml.YamlElement;
import kr.toxicity.hud.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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


    /**
     * Finds location from yaml.
     * @param data raw data
     * @return location
     */
    public static @NotNull PointedLocation deserialize(@NotNull YamlObject data) {
        return new PointedLocation(
                PointedLocationSource.INTERNAL,
                data.getAsString("name", "unknown"),
                Optional.ofNullable(data.get("icon")).map(YamlElement::asString).orElse(null),
                LocationWrapper.deserialize(Objects.requireNonNull(data.get("location"), "location").asObject())
        );
    }
    /**
     * Finds location from json.
     * @param data raw data
     * @return location
     */
    public static @NotNull PointedLocation deserialize(@NotNull JsonObject data) {
        return new PointedLocation(
                PointedLocationSource.INTERNAL,
                Optional.ofNullable(data.getAsJsonPrimitive("name")).map(JsonPrimitive::getAsString).orElse("unknown"),
                Optional.ofNullable(data.getAsJsonPrimitive("icon")).map(JsonPrimitive::getAsString).orElse(null),
                LocationWrapper.deserialize(Objects.requireNonNull(data.getAsJsonObject("location"), "location"))
        );
    }
    /**
     * Serializes to map.
     * @return map
     */
    public @NotNull Map<String, Object> serialize() {
        var map = new LinkedHashMap<String, Object>();
        map.put("name", name);
        if (icon != null) map.put("icon", icon);
        map.put("location", location.serialize());
        return map;
    }
}
