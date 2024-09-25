package kr.toxicity.hud.api;

import org.jetbrains.annotations.NotNull;

public interface BetterHudLogger {
    /**
     * Logs info level log.
     * @param message message
     */
    void info(@NotNull String... message);

    /**
     * Logs warn level log.
     * @param message message
     */
    void warn(@NotNull String... message);
}
