package kr.toxicity.hud.api.trgger;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Represents the wrapped bukkit event.
 * @param <T> event type
 */
public interface HudBukkitEventTrigger<T extends Event> extends HudTrigger<T> {
    /**
     * Returns the type.
     * @return event type
     */
    @NotNull Class<T> getEventClass();

    /**
     * Creates new wrapped trigger.
     * @param clazz event class
     * @param mapper mapper
     * @return trigger
     * @param <E> event type
     */
    static <E extends Event> HudBukkitEventTrigger<E> of(@NotNull Class<E> clazz, @NotNull Function<E, UUID> mapper) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(mapper);
        return new HudBukkitEventTrigger<>() {
            @Override
            public @NotNull Class<E> getEventClass() {
                return clazz;
            }

            @Override
            public @Nullable UUID getValue(E e) {
                return mapper.apply(e);
            }

            @Override
            public @NotNull UUID getKey(E e) {
                return UUID.randomUUID();
            }
        };
    }
    /**
     * Creates new wrapped trigger.
     * @param clazz event class
     * @return trigger
     * @param <E> event type
     */
    static <E extends PlayerEvent> HudBukkitEventTrigger<E> of(@NotNull Class<E> clazz) {
        Objects.requireNonNull(clazz);
        return new HudBukkitEventTrigger<>() {
            @Override
            public @NotNull Class<E> getEventClass() {
                return clazz;
            }

            @Override
            public @NotNull UUID getValue(@NotNull E e) {
                return e.getPlayer().getUniqueId();
            }

            @Override
            public @NotNull UUID getKey(@NotNull E e) {
                return UUID.randomUUID();
            }
        };
    }
}
