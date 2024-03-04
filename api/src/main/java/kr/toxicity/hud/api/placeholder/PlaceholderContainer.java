package kr.toxicity.hud.api.placeholder;

import org.jetbrains.annotations.NotNull;

public interface PlaceholderContainer<T> {
    void addPlaceholder(@NotNull String name, @NotNull HudPlaceholder<T> placeholder);
}
