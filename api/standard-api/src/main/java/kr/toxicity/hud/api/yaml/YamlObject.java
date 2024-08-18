package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public interface YamlObject extends Iterable<Map.Entry<String, YamlElement>>, YamlElement {

    @NotNull Map<String, Object> get();

    @Nullable YamlElement get(@NotNull String path);

    void merge(@NotNull YamlObject object);
    void save(@NotNull File file);

    default @NotNull String asString() {
        throw new UnsupportedOperationException("object");
    }
    default int asInt() {
        throw new UnsupportedOperationException("object");
    }
    default float asFloat() {
        throw new UnsupportedOperationException("object");
    }
    default double asDouble() {
        throw new UnsupportedOperationException("object");
    }
    default boolean asBoolean() {
        throw new UnsupportedOperationException("object");
    }
    default long asLong() {
        throw new UnsupportedOperationException("object");
    }
    default @NotNull YamlArray asArray() {
        throw new UnsupportedOperationException("object");
    }
    default @NotNull YamlObject asObject() {
        return this;
    }

    default boolean getAsBoolean(@NotNull String path, boolean defaultValue) {
        var value = get(path);
        return value != null ? value.asBoolean() : defaultValue;
    }
    default float getAsFloat(@NotNull String path, float defaultValue) {
        var value = get(path);
        return value != null ? value.asFloat() : defaultValue;
    }
    default int getAsInt(@NotNull String path, int defaultValue) {
        var value = get(path);
        return value != null ? value.asInt() : defaultValue;
    }
    default double getAsDouble(@NotNull String path, double defaultValue) {
        var value = get(path);
        return value != null ? value.asDouble() : defaultValue;
    }
    default long getAsLong(@NotNull String path, long defaultValue) {
        var value = get(path);
        return value != null ? value.asLong() : defaultValue;
    }
    default @NotNull String getAsString(@NotNull String path, @NotNull String defaultValue) {
        var value = get(path);
        return value != null ? value.asString() : defaultValue;
    }
}
