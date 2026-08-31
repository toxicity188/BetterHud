package kr.toxicity.hud.api.mod.trigger;

import kr.toxicity.hud.api.mod.event.EventRegistry;
import kr.toxicity.hud.api.mod.event.ModEvent;
import kr.toxicity.hud.api.trigger.HudTrigger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Mod platform trigger.
 * @param <T> registry type
 */
public interface HudModEventTrigger<T extends ModEvent<?>> extends HudTrigger<T> {
    /**
     * Gets event registry
     * @return registry
     */
    @NotNull EventRegistry<T> registry();
}
