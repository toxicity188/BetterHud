package kr.toxicity.hud.api.configuration;

import kr.toxicity.hud.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface HudComponentSupplier<T extends HudObject> extends Supplier<List<WidthComponent>> {
    @NotNull T parent();

    static <R extends HudObject> HudComponentSupplier<R> empty(@NotNull R parent) {
        return new HudComponentSupplier<>() {
            @Override
            public @NotNull R parent() {
                return parent;
            }

            @Override
            public List<WidthComponent> get() {
                return Collections.emptyList();
            }
        };
    }
    static <R extends HudObject> HudComponentSupplier<R> of(@NotNull R parent, @NotNull Supplier<List<WidthComponent>> supplier) {
        return new HudComponentSupplier<>() {
            @Override
            public @NotNull R parent() {
                return parent;
            }

            @Override
            public List<WidthComponent> get() {
                return supplier.get();
            }
        };
    }
}
