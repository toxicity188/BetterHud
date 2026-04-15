package kr.toxicity.hud.api.mod.update;

import kr.toxicity.hud.api.mod.event.ModEvent;
import kr.toxicity.hud.api.update.UpdateEvent;
import kr.toxicity.hud.api.update.UpdateReason;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapped event of Mod.
 * @param event mod event
 * @see ModEvent
 * @see kr.toxicity.hud.api.mod.event.EventRegistry
 * @param key unique key
 */
public record ModUpdateEvent(@NotNull ModEvent<?> event, @NotNull Object key) implements UpdateEvent {

    @Override
    public @NotNull UpdateReason getType() {
        return UpdateReason.FABRIC_EVENT;
    }

    @Override
    public @NotNull Object getKey() {
        return key;
    }
}
