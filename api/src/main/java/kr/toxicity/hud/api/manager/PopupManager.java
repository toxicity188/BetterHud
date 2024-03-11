package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.popup.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public interface PopupManager {
    @Nullable Popup getPopup(@NotNull String name);
    @NotNull @Unmodifiable Set<String> getAllNames();
    @NotNull @Unmodifiable Set<Popup> getDefaultPopups();
}
