package kr.toxicity.hud.api.fabric.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventRegistry<T> {

    public static final Unit UNIT = new Unit();

    public static final class Unit {
        private Unit() {
        }
    }

    private final List<Consumer<? super T>> registry = new ArrayList<>();

    public @NotNull EventRegistry<T> register(@NotNull Consumer<? super T> consumer) {
        registry.add(consumer);
        return this;
    }

    @ApiStatus.Internal
    public void call(@NotNull T t) {
        for (Consumer<? super T> consumer : registry) {
            consumer.accept(t);
        }
    }
}
