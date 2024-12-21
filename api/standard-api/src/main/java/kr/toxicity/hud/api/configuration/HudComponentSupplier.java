package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Gets component supplier.
 * @param <T> parent type
 */
public interface HudComponentSupplier<T extends HudObject> extends Supplier<List<WidthComponent>> {
    /**
     * Gets parent.
     * @return parent
     */
    @NotNull T parent();

    /**
     * Creates empty supplier by parent
     * @param parent parent
     * @return empty list supplier
     * @param <R> type
     */
    @ApiStatus.Internal
    static <R extends HudObject> @NotNull HudComponentSupplier<R> empty(@NotNull R parent) {
        return new HudComponentSupplier<>() {
            @Override
            public @NotNull R parent() {
                return parent;
            }

            @Override
            public @NotNull @Unmodifiable List<WidthComponent> get() {
                return Collections.emptyList();
            }
        };
    }
    /**
     * Creates supplier by parent
     * @param parent parent
     * @param supplier supplier
     * @return list supplier
     * @param <R> type
     */
    static <R extends HudObject> @NotNull HudComponentSupplier<R> of(@NotNull R parent, @NotNull Supplier<List<WidthComponent>> supplier) {
        return new HudComponentSupplier<>() {
            @Override
            public @NotNull R parent() {
                return parent;
            }

            @Override
            public @NotNull @Unmodifiable List<WidthComponent> get() {
                return supplier.get();
            }
        };
    }
}
