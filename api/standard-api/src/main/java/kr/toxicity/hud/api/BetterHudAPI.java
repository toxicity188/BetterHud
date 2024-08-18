package kr.toxicity.hud.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BetterHudAPI {

    private BetterHudAPI() {
        throw new RuntimeException();
    }

    private static BetterHud bootstrap;

    public static @NotNull BetterHud inst() {
        return Objects.requireNonNull(bootstrap);
    }

    public static void inst(@NotNull BetterHud instance) {
        if (bootstrap != null) throw new RuntimeException();
        bootstrap = instance;
    }
}
