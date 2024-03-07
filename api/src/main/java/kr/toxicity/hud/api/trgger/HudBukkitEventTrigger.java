package kr.toxicity.hud.api.trgger;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

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
            public @NotNull Function<E, UUID> getValueMapper() {
                return mapper;
            }

            @Override
            public @NotNull Function<? super E, UUID> getKeyMapper() {
                return e -> UUID.randomUUID();
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
            public @NotNull Function<E, UUID> getValueMapper() {
                return e -> e.getPlayer().getUniqueId();
            }

            @Override
            public @NotNull Function<? super E, UUID> getKeyMapper() {
                return e -> UUID.randomUUID();
            }
        };
    }
}
