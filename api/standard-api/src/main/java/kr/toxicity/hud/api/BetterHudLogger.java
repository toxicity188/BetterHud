package kr.toxicity.hud.api;

import org.jetbrains.annotations.NotNull;

public interface BetterHudLogger {
    void info(@NotNull String... message);
    void warn(@NotNull String... message);
}
