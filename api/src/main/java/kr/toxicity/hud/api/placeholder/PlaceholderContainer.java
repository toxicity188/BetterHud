package kr.toxicity.hud.api.placeholder;

import org.jetbrains.annotations.NotNull;

/**
 * Represents placeholder group.
 * @param <T> type of placeholder's return value.
 */
public interface PlaceholderContainer<T> {
    /**
     * Adds placeholder.
     * @param name id
     * @param placeholder placeholder
     */
    void addPlaceholder(@NotNull String name, @NotNull HudPlaceholder<T> placeholder);
}
