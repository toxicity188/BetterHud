package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.popup.Popup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Represents popup manager.
 */
public interface PopupManager {
    /**
     * Gets popup by given name.
     * @param name id
     * @return popup or null
     */
    @Nullable Popup getPopup(@NotNull String name);

    /**
     * Gets all name of popup.
     * @return names
     */
    @NotNull @Unmodifiable Set<String> getAllNames();

    /**
     * Gets all default popup.
     * @return default popups
     */
    @NotNull @Unmodifiable Set<Popup> getDefaultPopups();

    /**
     * Gets all popup.
     * @return all popups
     */
    @NotNull @Unmodifiable Set<Popup> getAllPopups();
}
