package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface YamlArray extends Iterable<YamlElement>, YamlElement {

    @NotNull
    List<Object> get();

    default @NotNull String asString() {
        throw new UnsupportedOperationException("array");
    }
    default int asInt() {
        throw new UnsupportedOperationException("array");
    }
    default float asFloat() {
        throw new UnsupportedOperationException("array");
    }
    default double asDouble() {
        throw new UnsupportedOperationException("array");
    }
    default boolean asBoolean() {
        throw new UnsupportedOperationException("array");
    }
    default long asLong() {
        throw new UnsupportedOperationException("array");
    }
    default @NotNull YamlArray asArray() {
        return this;
    }
    default @NotNull YamlObject asObject() {
        throw new UnsupportedOperationException("array");
    }
}
