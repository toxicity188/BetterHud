package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface PopupIterator extends Comparable<PopupIterator> {
    boolean isUnique();
    boolean markedAsRemoval();
    int getIndex();
    int getMaxIndex();
    void setIndex(int index);
    boolean available();
    boolean alwaysCheckCondition();
    void remove();
    boolean canSave();
    @NotNull PopupSortType getSortType();
    @NotNull Object getKey();
    @NotNull @Unmodifiable List<WidthComponent> next();
    @NotNull String name();
    int getPriority();
}
