package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Popup {
    @NotNull String getName();
    @NotNull String getGroupName();
    int getMaxStack();
    boolean isDefault();
    @Nullable PopupUpdater show(@NotNull UpdateEvent reason, @NotNull HudPlayer player);
    default boolean hide(@NotNull HudPlayer player) {
        var group = player.getPopupGroupIteratorMap().remove(getGroupName());
        if (group != null) {
            group.clear();
            return true;
        } else return false;
    }
    default int getLastIndex() {
        return getMaxStack() - 1;
    }
    default @Nullable PopupUpdater show(@NotNull UpdateEvent reason, @NotNull Player player) {
        return show(reason, BetterHud.getInstance().getHudPlayer(player));
    }
}
