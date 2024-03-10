package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface PopupIteratorGroup {
    @NotNull @Unmodifiable List<WidthComponent> next();
    int getIndex();
    void addIterator(@NotNull PopupIterator iterator);
    void clear();
    boolean contains(@NotNull String name);
}
