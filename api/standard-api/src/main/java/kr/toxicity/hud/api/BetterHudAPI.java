package kr.toxicity.hud.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BetterHudAPI {

    //Private constructor for dummy class.
    private BetterHudAPI() {
        throw new RuntimeException();
    }

    private static BetterHud main; //Main instance

    /**
     * Gets a main instance of BetterHud.
     * @return BetterHud
     */
    public static @NotNull BetterHud inst() {
        return Objects.requireNonNull(main);
    }

    @ApiStatus.Internal
    public static void inst(@NotNull BetterHud instance) {
        if (main != null) throw new RuntimeException();
        main = instance;
    }
}
