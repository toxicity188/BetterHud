package kr.toxicity.hud.api.placeholder;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents placeholder
 * @param <T> returns value's type
 */
public interface HudPlaceholder<T> {
    /**
     * Creates new function from given args.
     * @param args args
     * @param reason event
     * @throws RuntimeException if given args or reason is invalid.
     * @return function
     */
    @NotNull Function<HudPlayer, T> invoke(@NotNull @Unmodifiable List<String> args, @NotNull UpdateEvent reason);
    /**
     * Gets a length of required args.
     * @return args length.
     */
    int getRequiredArgsLength();

    /**
     * Creates new placeholder by function.
     * @param biFunction builder
     * @return an instance of placeholder.
     * @param <T> return value's type.
     */
    static <T> @NotNull HudPlaceholder<T> of(@NotNull BiFunction<List<String>, UpdateEvent, Function<HudPlayer, T>> biFunction) {
        Objects.requireNonNull(biFunction);
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
