package kr.toxicity.hud.api.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents volatile code handler.
 */
public interface NMS {
    /**
     * Injects player.
     * This method calls when player joined.
     * @param player target player
     * @param color bar's color
     */
    void inject(@NotNull Player player, @NotNull BarColor color);
    /**
     * Shows bar to some player.
     * @param player target player
     * @param color bar's color
     * @param component bar's title
     */
    void showBossBar(@NotNull Player player, @NotNull BarColor color, @NotNull Component component);

    /**
     * Removes bar from player.
     * @param player target player
     */
    void removeBossBar(@NotNull Player player);

    /**
     * Gets an instance that allows async access to getHandle()
     * @param player target player
     * @return adapted player
     */
    @NotNull Player getFoliaAdaptedPlayer(@NotNull Player player);

    /**
     * Gets that server's version.
     * @return version
     */
    @NotNull NMSVersion getVersion();

    /**
     * Gets textures value of player's game profile.
     * @param player target player
     * @return textures value
     */
    @NotNull String getTextureValue(@NotNull Player player);
}
