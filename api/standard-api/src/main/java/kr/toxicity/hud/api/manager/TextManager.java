package kr.toxicity.hud.api.manager;

import org.jetbrains.annotations.NotNull;

public interface TextManager {
    int getWidth(@NotNull String textName, double scale, @NotNull String text);
}
