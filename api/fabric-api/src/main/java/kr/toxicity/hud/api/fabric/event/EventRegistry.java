package kr.toxicity.hud.api.fabric.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventRegistry<T> {

    /**
     * All registries
     */
    private static final List<EventRegistry<?>> REGISTRIES = new ArrayList<>();

    /**
     * Unit instance
     */
    public static final Unit UNIT = new Unit();

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

    @ApiStatus.Internal
    public @NotNull EventRegistry<T> registerTemp(@NotNull Consumer<? super T> consumer) {
        tempRegistry.add(consumer);
        return this;
    }

    @ApiStatus.Internal
    public void clear() {
        tempRegistry.clear();
    }

    @ApiStatus.Internal
    public static void clearAll() {
        REGISTRIES.forEach(EventRegistry::clear);
    }

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
