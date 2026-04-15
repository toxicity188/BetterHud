package kr.toxicity.hud.api.placeholder;

import kr.toxicity.hud.api.player.HudPlayer;
import kr.toxicity.hud.api.update.UpdateEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents placeholder
 * @param <T> type (Number, String, Boolean)
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
    static <T> @NotNull HudPlaceholder<T> of(@NotNull PlaceholderFunction<T> biFunction) {
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

    /**
     * Creates placeholder builder.
     * @return builder
     * @param <T> type (Number, String, Boolean)
     */
    static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Placeholder builder
     * @param <T> type (Number, String, Boolean)
     */
    @Getter
    class Builder<T> {
        private int requiredArgsLength = 0;
        private PlaceholderFunction<T> function = (l, u) -> {
            throw new RuntimeException();
        };

        private Builder() {
        }

        /**
         * Sets required args amount
         * @param requiredArgsLength amount
         * @return this
         */
        public @NotNull Builder<T> requiredArgsLength(int requiredArgsLength) {
            this.requiredArgsLength = requiredArgsLength;
            return this;
        }

        /**
         * Sets function
         * @param function function
         * @return this
         */
        public @NotNull Builder<T> function(@NotNull PlaceholderFunction<T> function) {
            this.function = Objects.requireNonNull(function, "function");
            return this;
        }

        /**
         * Builds placeholder.
         * @return result
         */
        public @NotNull HudPlaceholder<T> build() {
            return new HudPlaceholder<>() {
                @Override
                public @NotNull Function<HudPlayer, T> invoke(@NotNull @Unmodifiable List<String> args, @NotNull UpdateEvent reason) {
                    return function.apply(args, reason);
                }

                @Override
                public int getRequiredArgsLength() {
                    return requiredArgsLength;
                }
            };
        }

        /**
         * Adds new placeholder to registry
         * @param name placeholder name
         * @param container registry
         * @see kr.toxicity.hud.api.manager.PlaceholderManager
         */
        public void add(@NotNull String name, @NotNull PlaceholderContainer<T> container) {
            container.addPlaceholder(name, build());
        }
    }

    /**
     * An aliases of (args, event) -> (player) -> T
     * @param <T> type (Number, String, Boolean)
     */
    @FunctionalInterface
    interface PlaceholderFunction<T> extends BiFunction<List<String>, UpdateEvent, Function<HudPlayer, T>> {
        /**
         * Gets placeholder function from single function
         * @param function function
         * @return placeholder function
         * @param <T> type (Number, String, Boolean)
         */
        static <T> @NotNull PlaceholderFunction<T> of(@NotNull Function<HudPlayer, T> function) {
            return (l, u) -> function;
        }
    }
}
