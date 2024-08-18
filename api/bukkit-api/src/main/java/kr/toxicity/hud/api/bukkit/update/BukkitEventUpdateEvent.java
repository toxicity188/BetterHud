package kr.toxicity.hud.api.bukkit.update;

import kr.toxicity.hud.api.update.UpdateEvent;
import kr.toxicity.hud.api.update.UpdateReason;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents wrapped event of bukkit
 * @param event original event
 * @param key event's key
 */
public record BukkitEventUpdateEvent(@NotNull Event event, @NotNull Object key) implements UpdateEvent {
    @Override
    public @NotNull UpdateReason getType() {
        return UpdateReason.BUKKIT_EVENT;
    }

    @Override
    public @NotNull Object getKey() {
        return key;
    }
}
