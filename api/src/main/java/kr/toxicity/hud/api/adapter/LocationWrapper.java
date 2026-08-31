package kr.toxicity.hud.api.adapter;

import com.google.gson.JsonObject;
import kr.toxicity.hud.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * A wrapper class of location.
 * @param world world
 * @param x x coordinate
 * @param y y coordinate
 * @param z z coordinate
 * @param pitch player's pitch
 * @param yaw player's yaw
 */
public record LocationWrapper(
        @NotNull WorldWrapper world,
        double x,
        double y,
        double z,
        float pitch,
        float yaw
) {
    /**
     * Finds location from YAML.
     * @param data raw data
     * @return location
     */
    public static @NotNull LocationWrapper deserialize(@NotNull YamlObject data) {
        return new LocationWrapper(
                WorldWrapper.deserialize(Objects.requireNonNull(data.get("world"), "world").asObject()),
                data.getAsDouble("x", 0),
                data.getAsDouble("y", 0),
                data.getAsDouble("z", 0),
                data.getAsFloat("pitch", 0),
                data.getAsFloat("yaw", 0)
        );
    }
    /**
     * Finds location from JSON.
     * @param data raw data
     * @return location
     */
    public static @NotNull LocationWrapper deserialize(@NotNull JsonObject data) {
        return new LocationWrapper(
                WorldWrapper.deserialize(Objects.requireNonNull(data.getAsJsonObject("world"), "world")),
                data.getAsJsonPrimitive("x").getAsDouble(),
                data.getAsJsonPrimitive("y").getAsDouble(),
                data.getAsJsonPrimitive("z").getAsDouble(),
                data.getAsJsonPrimitive("pitch").getAsFloat(),
                data.getAsJsonPrimitive("yaw").getAsFloat()
        );
    }
    /**
     * Serializes to map.
     * @return map
     */
    public @NotNull Map<String, Object> serialize() {
        return Map.ofEntries(
                Map.entry("world", world.serialize()),
                Map.entry("x", x),
                Map.entry("y", y),
                Map.entry("z", z),
                Map.entry("pitch", pitch),
                Map.entry("yaw", yaw)
        );
    }
}
