package kr.toxicity.hud.api.placeholder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

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

    /**
     * Gets all placeholders.
     * @return all placeholders
     */
    @NotNull
    @Unmodifiable
    Map<String, HudPlaceholder<?>> getAllPlaceholders();
}
