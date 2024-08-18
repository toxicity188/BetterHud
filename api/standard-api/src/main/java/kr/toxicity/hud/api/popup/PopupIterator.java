package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.UUID;

/**
 * Represents popup iterator.
 */
public interface PopupIterator extends Comparable<PopupIterator> {
    /**
     * Returns whether this iterator is unique
     * @return whether to unique
     */
    boolean isUnique();

    /**
     * Returns whether this popup is marked as removal.
     * @return whether to removal
     */
    boolean markedAsRemoval();

    /**
     * Gets the index of iterator.
     * @return index
     */
    int getIndex();

    /**
     * Gets the max index of iterator.
     * @return index
     */
    int getMaxIndex();

    /**
     * Sets the index of iterator.
     * @param index index
     */
    void setIndex(int index);

    /**
     * Checks this popup is available.
     * @return whether to available
     */
    boolean available();

    /**
     * Returns whether this popup always checks condition.
     * @return whether to always check
     */
    boolean alwaysCheckCondition();

    /**
     * Removes this iterator
     */
    void remove();

    /**
     * Returns this popup can save.
     * @return can save or not
     */
    boolean canSave();

    /**
     * Returns uuid.
     * @return uuid
     */
    @NotNull UUID getUUID();

    /**
     * Returns sort type.
     * @return sort type
     */
    @NotNull PopupSortType getSortType();

    /**
     * Returns key.
     * It is normally uuid.
     * @return key
     */
    @NotNull Object getKey();

    /**
     * Iterates this and returns next value.
     * @return next component
     */
    @NotNull @Unmodifiable List<WidthComponent> next();

    /**
     * Gets name.
     * @return name
     */
    @NotNull String name();

    /**
     * Gets priority
     * @return priority
     */
    int getPriority();

    /**
     * Sets priority
     * @param priority priority
     */
    void setPriority(int priority);
}
