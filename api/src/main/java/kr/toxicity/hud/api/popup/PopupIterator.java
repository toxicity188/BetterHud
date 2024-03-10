package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface PopupIterator {
    int getIndex();
    int getMaxIndex();
    void setIndex(int index);
    boolean available();
    void remove();
    @NotNull PopupSortType getSortType();
    @NotNull Object getKey();
    @NotNull @Unmodifiable List<WidthComponent> next();
    @NotNull String name();
    int getPriority();
}
