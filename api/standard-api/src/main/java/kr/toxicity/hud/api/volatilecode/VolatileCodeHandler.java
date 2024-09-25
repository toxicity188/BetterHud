package kr.toxicity.hud.api.volatilecode;

import kr.toxicity.hud.api.player.HudPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface VolatileCodeHandler {
    /**
     * Injects hudPlayer.
     * This method calls when HudPlayer joined.
     * @param hudPlayer target HudPlayer
     * @param color bar's color
     */
    void inject(@NotNull HudPlayer hudPlayer, @NotNull BossBar.Color color);
    /**
     * Shows bar to some hudPlayer.
     * @param hudPlayer target HudPlayer
     * @param color bar's color
     * @param component bar's title
     */
    void showBossBar(@NotNull HudPlayer hudPlayer, @NotNull BossBar.Color color, @NotNull Component component);

    /**
     * Removes bar from hudPlayer.
     * @param hudPlayer target HudPlayer
     */
    void removeBossBar(@NotNull HudPlayer hudPlayer);


    /**
     * Gets textures value of player's game profile.
     * @param hudPlayer target HudPlayer
     * @return textures value
     */
    @NotNull String getTextureValue(@NotNull HudPlayer hudPlayer);

    /**
     * Reloads player's boss bar.
     * @param hudPlayer target HudPlayer
     * @param color color
     */
    void reloadBossBar(@NotNull HudPlayer hudPlayer, @NotNull BossBar.Color color);
}
