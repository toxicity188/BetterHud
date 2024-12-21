package kr.toxicity.hud.api.adapter;

import com.google.gson.JsonObject;
import kr.toxicity.hud.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A wrapper class of world
 * @param name world name
 * @param uuid world uuid
 */
public record WorldWrapper(
        @NotNull String name,
        @NotNull UUID uuid
) {
    /**
     * Finds world from yaml.
     * @param data raw data
     * @return location
     */
    public static @NotNull WorldWrapper deserialize(@NotNull YamlObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.get("name"), "name").asString(),
                UUID.fromString(Objects.requireNonNull(data.get("uuid"), "uuid").asString())
        );
    }
    /**
     * Finds world from json.
     * @param data raw data
     * @return location
     */
    public static @NotNull WorldWrapper deserialize(@NotNull JsonObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.getAsJsonPrimitive("name"), "name").getAsString(),
                UUID.fromString(Objects.requireNonNull(data.getAsJsonPrimitive("uuid"), "uuid").getAsString())
        );
    }
    /**
     * Serializes to map.
     * @return map
     */
    public @NotNull Map<String, Object> serialize() {
        return Map.ofEntries(
                Map.entry("name", name),
                Map.entry("uuid", uuid.toString())
        );
    }
}
