package kr.toxicity.hud.api.volatilecode;

import kr.toxicity.hud.api.player.HudPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Version-volatile code.
 */
@ApiStatus.Internal
public interface VolatileCodeHandler {
    /**
     * Injects player.
     * This method calls when player joined.
     * @param player target player
     * @param color bar's color
     */
    void inject(@NotNull HudPlayer player, @NotNull BossBar.Color color);
    /**
     * Shows bar to some player.
     * @param player target player
     * @param color bar's color
     * @param component bar's title
     */
    void showBossBar(@NotNull HudPlayer player, @NotNull BossBar.Color color, @NotNull Component component);

    /**
     * Removes bar from player.
     * @param player target player
     */
    void removeBossBar(@NotNull HudPlayer player);


    /**
     * Gets textures value of player's game profile.
     * @param player target player
     * @return textures value
     */
    @NotNull String getTextureValue(@NotNull HudPlayer player);

    /**
     * Reloads player's boss bar.
     * @param player target player
     * @param color color
     */
    void reloadBossBar(@NotNull HudPlayer player, @NotNull BossBar.Color color);
}
