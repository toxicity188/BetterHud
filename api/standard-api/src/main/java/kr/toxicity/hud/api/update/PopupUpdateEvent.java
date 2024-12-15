package kr.toxicity.hud.api.update;

import kr.toxicity.hud.api.popup.PopupIterator;
import org.jetbrains.annotations.NotNull;

public record PopupUpdateEvent(@NotNull UpdateEvent source, @NotNull PopupIterator iterator) implements UpdateEvent {
    @Override
    public @NotNull UpdateReason getType() {
        return source.getType();
    }

    @Override
    public @NotNull Object getKey() {
        return source.getKey();
    }
}
