package kr.toxicity.hud.api.mod.event;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Entity event
 * @param <T> registry type
 */
public interface EntityEvent<T extends ModEvent<?>> extends ModEvent<T> {
    @NotNull LivingEntity entity();
}
