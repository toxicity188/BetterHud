package kr.toxicity.hud.api.adapter;

import com.google.gson.JsonObject;
import kr.toxicity.hud.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * A wrapper class of a world
 * @param name world name
 */
public record WorldWrapper(
        @NotNull String name
) {
    /**
     * Finds world from YAML.
     * @param data raw data
     * @return location
     */
    public static @NotNull WorldWrapper deserialize(@NotNull YamlObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.get("name"), "name").asString()
        );
    }
    /**
     * Finds world from JSON.
     * @param data raw data
     * @return location
     */
    public static @NotNull WorldWrapper deserialize(@NotNull JsonObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.getAsJsonPrimitive("name"), "name").getAsString()
        );
    }
    /**
     * Serializes to map.
     * @return map
     */
    public @NotNull Map<String, Object> serialize() {
        return Map.ofEntries(
                Map.entry("name", name)
        );
    }
}
