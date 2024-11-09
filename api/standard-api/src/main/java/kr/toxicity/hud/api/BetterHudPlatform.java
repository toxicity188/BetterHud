package kr.toxicity.hud.api;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public enum BetterHudPlatform {
    BUKKIT(b -> !b.isPaper() && !b.isFabric() && !b.isVelocity()),
    PAPER(BetterHudBootstrap::isPaper),
    VELOCITY(BetterHudBootstrap::isVelocity),
    FABRIC(BetterHudBootstrap::isFabric),
    ;

    public static final @NotNull @Unmodifiable List<BetterHudPlatform> ALL = Arrays.stream(values()).toList();

    private final @NotNull Predicate<BetterHudBootstrap> predicate;

    public boolean match(@NotNull BetterHudBootstrap bootstrap) {
        return predicate.test(bootstrap);
    }
}
