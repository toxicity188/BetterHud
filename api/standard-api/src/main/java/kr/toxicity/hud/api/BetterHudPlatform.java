package kr.toxicity.hud.api;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * BetterHud's platform.
 */
@RequiredArgsConstructor
public enum BetterHudPlatform {
    /**
     * Checks Bukkit (not Paper)
     */
    BUKKIT(b -> !b.isPaper() && !b.isFabric() && !b.isVelocity()),
    /**
     * Checks Paper
     */
    PAPER(BetterHudBootstrap::isPaper),
    /**
     * Checks Velocity
     */
    VELOCITY(BetterHudBootstrap::isVelocity),
    /**
     * Checks Fabric server
     */
    FABRIC(BetterHudBootstrap::isFabric),
    ;

    /**
     * All platform.
     */
    public static final @NotNull @Unmodifiable List<BetterHudPlatform> ALL = Arrays.stream(values()).toList();

    private final @NotNull Predicate<BetterHudBootstrap> predicate;

    /**
     * Checks platform matching
     * @param bootstrap BetterHud bootstrap
     * @return whether to match or nut
     */
    @ApiStatus.Internal
    public boolean match(@NotNull BetterHudBootstrap bootstrap) {
        return predicate.test(bootstrap);
    }
}
