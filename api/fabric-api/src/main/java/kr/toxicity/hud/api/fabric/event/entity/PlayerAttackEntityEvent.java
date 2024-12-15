package kr.toxicity.hud.api.fabric.event.entity;

import kr.toxicity.hud.api.fabric.event.EntityEvent;
import kr.toxicity.hud.api.fabric.event.EventRegistry;
import kr.toxicity.hud.api.fabric.event.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Player attacks entity.
 * @param player player
 * @param entity entity
 */
public record PlayerAttackEntityEvent(@NotNull ServerPlayer player, @NotNull LivingEntity entity) implements PlayerEvent<PlayerAttackEntityEvent>, EntityEvent<PlayerAttackEntityEvent> {
    /**
     * Event registry
     */
    public static final EventRegistry<PlayerAttackEntityEvent> REGISTRY = new EventRegistry<>();

    @Override
    public @NotNull EventRegistry<PlayerAttackEntityEvent> getRegistry() {
        return REGISTRY;
    }
}
