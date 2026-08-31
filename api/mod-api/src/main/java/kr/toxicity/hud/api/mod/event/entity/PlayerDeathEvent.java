package kr.toxicity.hud.api.mod.event.entity;

import kr.toxicity.hud.api.mod.event.EventRegistry;
import kr.toxicity.hud.api.mod.event.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * The Player is dead.
 * @param player player
 */
public record PlayerDeathEvent(@NotNull ServerPlayer player) implements PlayerEvent<PlayerDeathEvent> {
    /**
     * Event registry
     */
    public static final EventRegistry<PlayerDeathEvent> REGISTRY = new EventRegistry<>();

    @Override
    public @NotNull EventRegistry<PlayerDeathEvent> getRegistry() {
        return REGISTRY;
    }
}
