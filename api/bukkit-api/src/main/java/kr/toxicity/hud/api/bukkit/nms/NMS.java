package kr.toxicity.hud.api.bukkit.nms;

import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents volatile code handler.
 */
public interface NMS extends VolatileCodeHandler {
    /**
     * Gets an instance that allows async access to getHandle()
     * @param player target HudPlayer
     * @return adapted HudPlayer
     */
    @NotNull
    Player getFoliaAdaptedPlayer(@NotNull Player player);

    /**
     * Gets that server's version.
     * @return version
     */
    @NotNull NMSVersion getVersion();
}
