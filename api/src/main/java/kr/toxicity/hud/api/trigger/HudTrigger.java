package kr.toxicity.hud.api.trigger;

import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Represents the popup trigger
 * @param <T> event type
 */
public interface HudTrigger<T> {

    /**
     * Gets the unique key. normally random uuid.
     * @param t event
     * @return event's key
     */
    @NotNull Object getKey(T t);

    /**
     * Register some event handler at this trigger.
     * @param eventConsumer event handler
     */
    void registerEvent(@NotNull BiConsumer<UUID, UpdateEvent> eventConsumer);
}
