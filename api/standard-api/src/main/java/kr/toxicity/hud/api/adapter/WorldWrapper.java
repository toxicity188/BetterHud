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
    public static @NotNull WorldWrapper deserialize(@NotNull YamlObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.get("name"), "name").asString(),
                UUID.fromString(Objects.requireNonNull(data.get("uuid"), "uuid").asString())
        );
    }
    public static @NotNull WorldWrapper deserialize(@NotNull JsonObject data) {
        return new WorldWrapper(
                Objects.requireNonNull(data.getAsJsonPrimitive("name"), "name").getAsString(),
                UUID.fromString(Objects.requireNonNull(data.getAsJsonPrimitive("uuid"), "uuid").getAsString())
        );
    }
    public @NotNull Map<String, Object> serialize() {
        return Map.ofEntries(
                Map.entry("name", name),
                Map.entry("uuid", uuid.toString())
        );
    }
}
