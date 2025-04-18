package kr.toxicity.hud.api.fabric.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Event registry of BetterHud
 * @param <T> event type
 */
public final class EventRegistry<T> {

    /**
     * All registries
     */
    private static final List<EventRegistry<?>> REGISTRIES = new ArrayList<>();

    /**
     * Unit instance
     */
    public static final Unit UNIT = new Unit();

    /**
     * Unit class
     */
    public static final class Unit {
        private Unit() {
        }
    }

    {
        REGISTRIES.add(this);
    }

    private final List<Consumer<? super T>> registry = new ArrayList<>();
    private final List<Consumer<? super T>> tempRegistry = new ArrayList<>();

    /**
     * Registers listener
     * @param consumer listener
     * @return self
     */
    public @NotNull EventRegistry<T> register(@NotNull Consumer<? super T> consumer) {
        registry.add(consumer);
        return this;
    }

    /**
     * Registers temp listener
     * @param consumer listener
     * @return self
     */
    @ApiStatus.Internal
    public @NotNull EventRegistry<T> registerTemp(@NotNull Consumer<? super T> consumer) {
        tempRegistry.add(consumer);
        return this;
    }

    /**
     * Clears all temp listener.
     */
    @ApiStatus.Internal
    public void clear() {
        tempRegistry.clear();
    }

    /**
     * Clears all temp listeners from all registries.
     */
    @ApiStatus.Internal
    public static void clearAll() {
        REGISTRIES.forEach(EventRegistry::clear);
    }

    /**
     * Calls event.
     * @param t event
     */
    @ApiStatus.Internal
    public void call(@NotNull T t) {
        for (Consumer<? super T> consumer : registry) {
            consumer.accept(t);
        }
        for (Consumer<? super T> consumer : tempRegistry) {
            consumer.accept(t);
        }
    }
}
