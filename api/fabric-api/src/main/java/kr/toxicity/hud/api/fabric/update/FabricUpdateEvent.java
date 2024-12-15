package kr.toxicity.hud.api.fabric.update;

import kr.toxicity.hud.api.fabric.event.FabricEvent;
import kr.toxicity.hud.api.update.UpdateEvent;
import kr.toxicity.hud.api.update.UpdateReason;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapped event of Fabric.
 * @param event fabric event
 * @see FabricEvent
 * @see kr.toxicity.hud.api.fabric.event.EventRegistry
 * @param key unique key
 */
public record FabricUpdateEvent(@NotNull FabricEvent<?> event, @NotNull Object key) implements UpdateEvent {

    @Override
    public @NotNull UpdateReason getType() {
        return UpdateReason.FABRIC_EVENT;
    }

    @Override
    public @NotNull Object getKey() {
        return key;
    }
}
