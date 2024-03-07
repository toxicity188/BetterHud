package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.UUID;

public interface PopupIterator {
    int getIndex();
    void setIndex(int index);
    boolean available();
    void remove();
    @NotNull UUID getUUID();
    @NotNull @Unmodifiable List<WidthComponent> next();
    @NotNull String name();
    int getPriority();
}
