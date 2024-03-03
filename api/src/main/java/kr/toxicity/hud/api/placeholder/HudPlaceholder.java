package kr.toxicity.hud.api.placeholder;

import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.BiFunction;

public interface HudPlaceholder<T> {
    @NotNull T invoke(@NotNull HudPlayer player, @NotNull @Unmodifiable List<String> args);
    int getRequiredArgsLength();

    static <T> @NotNull HudPlaceholder<T> of(int length, BiFunction<HudPlayer, List<String>, T> biFunction) {
        return new HudPlaceholder<>() {
            @Override
            public @NotNull T invoke(@NotNull HudPlayer player, @NotNull @Unmodifiable List<String> args) {
                return biFunction.apply(player, args);
            }

            @Override
            public int getRequiredArgsLength() {
                return length;
            }
        };
    }
}
