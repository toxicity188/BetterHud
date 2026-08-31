package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents the group of iterator.
 */
public interface PopupIteratorGroup {
    /**
     * Gets all next value of iterator.
     * @return component
     */
    @NotNull @Unmodifiable List<WidthComponent> next();

    /**
     * Gets index
     * @return index
     */
    int getIndex();

    /**
     * Adds iterator.
     * @param iterator target iterator
     */
    void addIterator(@NotNull PopupIterator iterator);

    /**
     * Clears this group.
     */
    void clear();

    /**
     * Returns whether this name is contained
     * @param name name of iterator
     * @return whether this name is contained
     */
    boolean contains(@NotNull String name);

    /**
     * Returns whether this popup is contained
     * @param popup popup of iterator
     * @return whether this popup is contained
     */
    default boolean contains(@NotNull Popup popup) {
        return contains(popup.getName());
    }
}
