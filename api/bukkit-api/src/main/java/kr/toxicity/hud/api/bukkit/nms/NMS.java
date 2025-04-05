package kr.toxicity.hud.api.bukkit.nms;

import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.impl.CommandModule;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents volatile code handler.
 */
@ApiStatus.Internal
public interface NMS extends VolatileCodeHandler {
    /**
     * Gets an instance that allows async access to getHandle()
     * @param player target player
     * @return adapted player
     */
    @NotNull
    Player getFoliaAdaptedPlayer(@NotNull Player player);

    /**
     * Gets an instance that allows async access to getHandle()
     * @param entity target entity
     * @return adapted entity
     */
    @NotNull
    Entity getFoliaAdaptedEntity(@NotNull Entity entity);

    /**
     * Gets that server's version.
     * @return version
     */
    @NotNull NMSVersion getVersion();

    /**
     * Registers brigadier command to server.
     * @param module module
     */
    void registerCommand(@NotNull CommandModule<BetterCommandSource> module);

    /**
     * Registers this command on reload too.
     * @param module modules
     */
    void handleReloadCommand(@NotNull CommandModule<BetterCommandSource> module);
}
