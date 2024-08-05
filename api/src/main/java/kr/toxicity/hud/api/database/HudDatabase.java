package kr.toxicity.hud.api.database;

import kr.toxicity.hud.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * User data save class.
 */
public interface HudDatabase extends AutoCloseable {
    /**
     * Loads player's data.
     * @param player target player
     * @return loaded data
     */
    @NotNull HudPlayer load(@NotNull Player player);

    /**
     * Saves player's data
     * @param player target data
     * @return whether to success
     */
    boolean save(@NotNull HudPlayer player);

    /**
     * Returns whether this database is closed.
     * @return close or not
     */
    boolean isClosed();
}
