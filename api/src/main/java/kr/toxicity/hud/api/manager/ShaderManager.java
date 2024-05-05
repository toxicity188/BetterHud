package kr.toxicity.hud.api.manager;

import org.jetbrains.annotations.NotNull;

public interface ShaderManager {
    void addConstant(@NotNull String key, @NotNull String value);
}
