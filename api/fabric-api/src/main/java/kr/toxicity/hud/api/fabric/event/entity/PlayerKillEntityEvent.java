package kr.toxicity.hud.api.fabric.event.entity;

import kr.toxicity.hud.api.fabric.event.EntityEvent;
import kr.toxicity.hud.api.fabric.event.EventRegistry;
import kr.toxicity.hud.api.fabric.event.FabricEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Player kills entity
 * @param player player
 * @param entity entity
 */
public record PlayerKillEntityEvent(@NotNull ServerPlayer player, @NotNull LivingEntity entity) implements FabricEvent<PlayerKillEntityEvent>, EntityEvent<PlayerKillEntityEvent> {
    /**
     * Event registry
     */
    public static final EventRegistry<PlayerKillEntityEvent> REGISTRY = new EventRegistry<>();

    @Override
    public @NotNull EventRegistry<PlayerKillEntityEvent> getRegistry() {
        return REGISTRY;
    }
}
