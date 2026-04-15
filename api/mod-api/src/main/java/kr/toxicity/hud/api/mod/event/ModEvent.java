package kr.toxicity.hud.api.mod.event;

import org.jetbrains.annotations.NotNull;

/**
 * All supertype of Mod event.
 * @param <T> registry type
 */
public interface ModEvent<T extends ModEvent<?>> {
    /**
     * Gets registry of this event.
     * @return registry
     */
    @NotNull EventRegistry<T> getRegistry();
}
