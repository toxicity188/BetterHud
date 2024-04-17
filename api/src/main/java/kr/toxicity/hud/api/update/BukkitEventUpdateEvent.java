package kr.toxicity.hud.api.update;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
