package kr.toxicity.hud.api.fabric.event;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Player event
 * @param <T> registry type
 */
public interface PlayerEvent<T extends FabricEvent<?>> extends FabricEvent<T> {
    /**
     * Gets triggered player.
     * @return player
     */
    @NotNull ServerPlayer player();
}
