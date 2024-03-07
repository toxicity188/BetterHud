package kr.toxicity.hud.api.placeholder;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface HudPlaceholder<T> {
    @NotNull Function<HudPlayer, T> invoke(@NotNull @Unmodifiable List<String> args, @NotNull UpdateEvent reason);
    int getRequiredArgsLength();

    static <T> @NotNull HudPlaceholder<T> of(BiFunction<List<String>, UpdateEvent, Function<HudPlayer, T>> biFunction) {
        return new HudPlaceholder<>() {
            @Override
            public @NotNull Function<HudPlayer, T> invoke(@NotNull @Unmodifiable List<String> args, @NotNull UpdateEvent reason) {
                return biFunction.apply(args, reason);
            }

            @Override
            public int getRequiredArgsLength() {
                return 0;
            }
        };
    }
}
