package kr.toxicity.hud.api.bukkit.nms;

import kr.toxicity.command.BetterCommandSource;
import kr.toxicity.command.CommandModule;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents volatile code handler.
 */
public interface NMS extends VolatileCodeHandler {
    /**
     * Gets an instance that allows async access to getHandle()
     * @param player target player
     * @return adapted player
     */
    @NotNull
    Player getFoliaAdaptedPlayer(@NotNull Player player);

    /**
     * Gets that server's version.
     * @return version
     */
    @NotNull NMSVersion getVersion();

    void registerCommand(@NotNull CommandModule<BetterCommandSource> module);
    void syncCommands(@NotNull Player player);
}
