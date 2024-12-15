package kr.toxicity.hud.api.fabric.event;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Entity event
 * @param <T> registry type
 */
public interface EntityEvent<T extends FabricEvent<?>> extends FabricEvent<T> {
    @NotNull LivingEntity entity();
}
