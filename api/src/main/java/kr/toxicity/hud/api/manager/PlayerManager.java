package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.player.PointedLocationProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.UUID;

public interface PlayerManager {
    /**
     * Gets the player's data from bukkit player.
     * @param player target player
     * @return player's data
     */
    @NotNull
    HudPlayer getHudPlayer(@NotNull Player player);
    /**
     * Gets the player's data from bukkit player.
     * @param uuid target player's uuid
     * @return player's data
     */
    @Nullable
    HudPlayer getHudPlayer(@NotNull UUID uuid);

    /**
     * Gets all player's instance.
     * @return all player
     */
    @NotNull
    @Unmodifiable
    Collection<HudPlayer> getAllHudPlayer();

    /**
     * Adds some location provider for compass.
     * @param provider location provider
     */
    void addLocationProvider(@NotNull PointedLocationProvider provider);
}
