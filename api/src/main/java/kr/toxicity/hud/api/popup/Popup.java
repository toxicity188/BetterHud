package kr.toxicity.hud.api.popup;

import kr.toxicity.hud.api.MythicHud;
import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Popup {

    void show(@NotNull HudPlayer player);
    default void show(@NotNull Player player) {
        show(MythicHud.getInstance().getHudPlayer(player));
    }
}
