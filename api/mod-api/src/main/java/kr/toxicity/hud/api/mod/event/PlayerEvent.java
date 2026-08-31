package kr.toxicity.hud.api.mod.event;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Player event
 * @param <T> registry type
 */
public interface PlayerEvent<T extends ModEvent<?>> extends ModEvent<T> {
    /**
     * Gets triggered player.
     * @return player
     */
    @NotNull ServerPlayer player();
}
