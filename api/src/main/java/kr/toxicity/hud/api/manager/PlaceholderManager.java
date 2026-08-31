package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.placeholder.PlaceholderContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents placeholder manager.
 */
public interface PlaceholderManager {
    /**
     * Gets a number placeholder group.
     * @see PlaceholderContainer
     * @return number group.
     */
    @NotNull PlaceholderContainer<Number> getNumberContainer();
    /**
     * Gets a boolean placeholder group.
     * @see PlaceholderContainer
     * @return boolean group.
     */
    @NotNull PlaceholderContainer<Boolean> getBooleanContainer();
    /**
     * Gets a string placeholder group.
     * @see PlaceholderContainer
     * @return string group.
     */
    @NotNull PlaceholderContainer<String> getStringContainer();
}
