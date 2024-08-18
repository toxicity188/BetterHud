package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

public interface YamlConfiguration extends Comparable<YamlConfiguration> {
    @NotNull
    String path();

    @Override
    default int compareTo(@NotNull YamlConfiguration o) {
        return path().compareTo(o.path());
    }
}
