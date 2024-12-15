package kr.toxicity.hud.api.fabric.event.entity;

import kr.toxicity.hud.api.fabric.event.EntityEvent;
import kr.toxicity.hud.api.fabric.event.EventRegistry;
import kr.toxicity.hud.api.fabric.event.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Player damaged by entity.
 * @param player player
 * @param entity entity
 */
public record PlayerDamageByEntityEvent(@NotNull ServerPlayer player, @NotNull LivingEntity entity) implements PlayerEvent<PlayerDamageByEntityEvent>, EntityEvent<PlayerDamageByEntityEvent> {
    /**
     * Event registry
     */
    public static final EventRegistry<PlayerDamageByEntityEvent> REGISTRY = new EventRegistry<>();

    @Override
    public @NotNull EventRegistry<PlayerDamageByEntityEvent> getRegistry() {
        return REGISTRY;
    }
}
