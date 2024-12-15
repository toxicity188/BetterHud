package kr.toxicity.hud.api.fabric.trigger;

import kr.toxicity.hud.api.fabric.event.EventRegistry;
import kr.toxicity.hud.api.fabric.event.FabricEvent;
import kr.toxicity.hud.api.trigger.HudTrigger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Fabric platform trigger.
 * @param <T> registry type
 */
public interface HudFabricEventTrigger<T extends FabricEvent<?>> extends HudTrigger<T> {
    /**
     * Gets event registry
     * @return registry
     */
    @NotNull EventRegistry<T> registry();
}
