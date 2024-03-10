package kr.toxicity.hud.api.trgger;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public interface HudBukkitEventTrigger<T extends Event> extends HudTrigger<T> {
    @NotNull Class<T> getEventClass();

    static <E extends Event> HudBukkitEventTrigger<E> of(@NotNull Class<E> clazz, @NotNull Function<E, UUID> mapper) {
        Objects.requireNonNull(clazz);
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
    static <E extends PlayerEvent> HudBukkitEventTrigger<E> of(@NotNull Class<E> clazz) {
        Objects.requireNonNull(clazz);
        return new HudBukkitEventTrigger<>() {
            @Override
            public @NotNull Class<E> getEventClass() {
                return clazz;
            }

            @Override
            public @NotNull UUID getValue(E e) {
                return e.getPlayer().getUniqueId();
            }

            @Override
            public @NotNull UUID getKey(E e) {
                return UUID.randomUUID();
            }
        };
    }
}
