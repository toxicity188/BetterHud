package kr.toxicity.hud.api.fabric.event;

import org.jetbrains.annotations.NotNull;

/**
 * All supertype of fabric event.
 * @param <T> registry type
 */
public interface FabricEvent<T extends FabricEvent<?>> {
    /**
     * Gets registry of this event.
     * @return registry
     */
    @NotNull EventRegistry<T> getRegistry();
}
