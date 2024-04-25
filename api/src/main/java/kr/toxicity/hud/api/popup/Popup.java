package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.entity.Player;
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
     * Shows popup to some player.
     * @param reason causes
     * @param player target player
     * @return updater of popup or null if showing has failed.
     */
    @Nullable PopupUpdater show(@NotNull UpdateEvent reason, @NotNull HudPlayer player);

    /**
     * Hides popup to some player.
     * @param player target player
     * @return whether to success
     */
    default boolean hide(@NotNull HudPlayer player) {
        var group = player.getPopupGroupIteratorMap().remove(getGroupName());
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

    /**
     * Shows popup to some player.
     * @param reason causes
     * @param player target player
     * @return updater of popup or null if showing has failed.
     */
    default @Nullable PopupUpdater show(@NotNull UpdateEvent reason, @NotNull Player player) {
        return show(reason, BetterHud.getInstance().getHudPlayer(player));
    }
}
