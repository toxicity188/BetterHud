package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents popup.
 */
public interface Popup extends HudObject {

    /**
     * Gets a group name of popup.
     * @return group name.
     */
    @NotNull String getGroupName();

    /**
     * Gets a popup's max stack size
     * @return max stack size
     */
    int getMaxStack();

    /**
     * Shows popup to some hudPlayer.
     * @param reason causes
     * @param hudPlayer target HudPlayer
     * @return updater of popup or null if showing has failed.
     */
    @Nullable PopupUpdater show(@NotNull UpdateEvent reason, @NotNull HudPlayer hudPlayer);

    /**
     * Hides popup to some hudPlayer.
     * @param hudPlayer target HudPlayer
     * @return whether to success
     */
    default boolean hide(@NotNull HudPlayer hudPlayer) {
        var group = hudPlayer.getPopupGroupIteratorMap().remove(getGroupName());
        if (group != null) {
            group.clear();
            return true;
        } else return false;
    }

    /**
     * Gets a last index of popup.
     * It equals getMaxStackSize() - 1
     * @return last index
     */
    default int getLastIndex() {
        return getMaxStack() - 1;
    }
}