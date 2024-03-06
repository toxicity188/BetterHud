package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.popup.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PopupManager {
    @Nullable Popup getPopup(@NotNull String name);
}
