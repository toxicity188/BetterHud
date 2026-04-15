package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

/**
 * Yaml map.
 */
public interface YamlObject extends Iterable<Map.Entry<String, YamlElement>>, YamlElement {

    /**
     * Gets data as a map.
     * @return map
     */
    @NotNull Map<String, Object> get();

    /**
     * Gets YAML element by some key.
     * @param path key
     * @return element or null if not exists.
     */
    @Nullable YamlElement get(@NotNull String path);

    /**
     * Merges data with another object.
     * @param object another object
     */
    void merge(@NotNull YamlObject object);

    /**
     * Saves YAML as a flat file.
     * @param file target file
     */
    void save(@NotNull File file);

    /**
     * Gets this element as a string
     * @throws UnsupportedOperationException if type is different.
     * @return string
     */
    default @NotNull String asString() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as an int
     * @throws UnsupportedOperationException if type is different.
     * @return int
     */
    default int asInt() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as a float
     * @throws UnsupportedOperationException if type is different.
     * @return float
     */
    default float asFloat() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as a double
     * @throws UnsupportedOperationException if type is different.
     * @return double
     */
    default double asDouble() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as a boolean
     * @throws UnsupportedOperationException if type is different.
     * @return boolean
     */
    default boolean asBoolean() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as along
     * @throws UnsupportedOperationException if type is different.
     * @return long
     */
    default long asLong() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as an array
     * @throws UnsupportedOperationException if type is different.
     * @return array
     */
    default @NotNull YamlArray asArray() {
        throw new UnsupportedOperationException("object");
    }
    /**
     * Gets this element as an object
     * @throws UnsupportedOperationException if type is different.
     * @return object
     */
    default @NotNull YamlObject asObject() {
        return this;
    }

    /**
     * Gets some value by some key as a boolean
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default boolean getAsBoolean(@NotNull String path, boolean defaultValue) {
        var value = get(path);
        return value != null ? value.asBoolean() : defaultValue;
    }
    /**
     * Gets some value by some key as a float
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default float getAsFloat(@NotNull String path, float defaultValue) {
        var value = get(path);
        return value != null ? value.asFloat() : defaultValue;
    }
    /**
     * Gets some value by some key as an int
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default int getAsInt(@NotNull String path, int defaultValue) {
        var value = get(path);
        return value != null ? value.asInt() : defaultValue;
    }
    /**
     * Gets some value by some key as a double
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default double getAsDouble(@NotNull String path, double defaultValue) {
        var value = get(path);
        return value != null ? value.asDouble() : defaultValue;
    }
    /**
     * Gets some value by some key as along
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default long getAsLong(@NotNull String path, long defaultValue) {
        var value = get(path);
        return value != null ? value.asLong() : defaultValue;
    }
    /**
     * Gets some value by some key as a string
     * @throws UnsupportedOperationException if type is different.
     * @param path key
     * @param defaultValue default value
     * @return searched value or default value if null
     */
    default @NotNull String getAsString(@NotNull String path, @NotNull String defaultValue) {
        var value = get(path);
        return value != null ? value.asString() : defaultValue;
    }
}
