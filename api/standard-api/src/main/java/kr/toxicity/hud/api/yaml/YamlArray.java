package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface YamlArray extends Iterable<YamlElement>, YamlElement {

    /**
     * Gets an array as list.
     * @return raw list
     */
    @NotNull
    List<Object> get();

    /**
     * Gets this element as a string
     * @throws UnsupportedOperationException if type is different.
     * @return string
     */
    default @NotNull String asString() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as an int
     * @throws UnsupportedOperationException if type is different.
     * @return int
     */
    default int asInt() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as a float
     * @throws UnsupportedOperationException if type is different.
     * @return float
     */
    default float asFloat() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as a double
     * @throws UnsupportedOperationException if type is different.
     * @return double
     */
    default double asDouble() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as a boolean
     * @throws UnsupportedOperationException if type is different.
     * @return boolean
     */
    default boolean asBoolean() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as a long
     * @throws UnsupportedOperationException if type is different.
     * @return long
     */
    default long asLong() {
        throw new UnsupportedOperationException("array");
    }
    /**
     * Gets this element as an array
     * @throws UnsupportedOperationException if type is different.
     * @return array
     */
    default @NotNull YamlArray asArray() {
        return this;
    }
    /**
     * Gets this element as an object
     * @throws UnsupportedOperationException if type is different.
     * @return object
     */
    default @NotNull YamlObject asObject() {
        throw new UnsupportedOperationException("array");
    }
}
