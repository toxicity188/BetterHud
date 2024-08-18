package kr.toxicity.hud.api.trigger;

import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Represents the popup trigger
 * @param <T> event type
 */
public interface HudTrigger<T> {
    /**
     * Gets the player uuid.
     * @param t event
     * @return player's uuid or null.
     */
    @Nullable UUID getValue(T t);

    /**
     * Gets the unique key. normally random uuid.
     * @param t event
     * @return event's key
     */
    @NotNull Object getKey(T t);

    void registerEvent(@NotNull BiConsumer<UUID, UpdateEvent> eventConsumer);
}
