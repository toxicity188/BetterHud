package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

public interface YamlElement extends YamlConfiguration {
    @NotNull Object get();

    @NotNull String asString();
    int asInt();
    float asFloat();
    double asDouble();
    long asLong();
    boolean asBoolean();

    @NotNull YamlArray asArray();
    @NotNull YamlObject asObject();
}
