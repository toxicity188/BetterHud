package kr.toxicity.hud.api.database;

import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HudDatabase extends AutoCloseable {
    @NotNull HudPlayer load(@NotNull Player player);
    boolean save(@NotNull HudPlayer player);
}
