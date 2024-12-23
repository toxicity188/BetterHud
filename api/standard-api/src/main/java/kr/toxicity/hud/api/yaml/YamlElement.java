package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

/**
 * Yaml element.
 */
public interface YamlElement extends YamlConfiguration {
    /**
     * Gets original raw object
     * @return raw object
     */
    @NotNull Object get();

    /**
     * Gets this element as a string
     * @throws UnsupportedOperationException if type is different.
     * @return string
     */
    @NotNull String asString();
    /**
     * Gets this element as an int
     * @throws UnsupportedOperationException if type is different.
     * @return int
     */
    int asInt();
    /**
     * Gets this element as a float
     * @throws UnsupportedOperationException if type is different.
     * @return float
     */
    float asFloat();
    /**
     * Gets this element as a double
     * @throws UnsupportedOperationException if type is different.
     * @return double
     */
    double asDouble();
    /**
     * Gets this element as a long
     * @throws UnsupportedOperationException if type is different.
     * @return long
     */
    long asLong();
    /**
     * Gets this element as a boolean
     * @throws UnsupportedOperationException if type is different.
     * @return boolean
     */
    boolean asBoolean();

    /**
     * Gets this element as an array
     * @throws UnsupportedOperationException if type is different.
     * @return array
     */
    @NotNull YamlArray asArray();
    /**
     * Gets this element as an object
     * @throws UnsupportedOperationException if type is different.
     * @return object
     */
    @NotNull YamlObject asObject();
}
