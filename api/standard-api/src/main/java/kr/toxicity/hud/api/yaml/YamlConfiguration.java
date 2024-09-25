package kr.toxicity.hud.api.yaml;

import org.jetbrains.annotations.NotNull;

public interface YamlConfiguration extends Comparable<YamlConfiguration> {

    /**
     * Gets this configuration's path
     * @return path
     */
    @NotNull
    String path();

    @Override
    default int compareTo(@NotNull YamlConfiguration o) {
        return path().compareTo(o.path());
    }
}
