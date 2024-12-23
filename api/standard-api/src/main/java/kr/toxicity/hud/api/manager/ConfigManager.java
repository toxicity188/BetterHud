package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.BetterHudAPI;
import org.jetbrains.annotations.NotNull;

/**
 * Config manager
 */
public interface ConfigManager {
    /**
     * Gets used bossbar line
     * @return line
     */
    int getBossbarLine();

    /**
     * Gets whether to enable debug or not
     * @return whether to enable debug or not
     */
    boolean isDebug();
    /**
     * Gets debug level
     * @return level
     */
    @NotNull DebugLevel getDebugLevel();

    /**
     * Debug level
     */
    enum DebugLevel {
        /**
         * Shows manager reload
         */
        MANAGER,
        /**
         * Shows assets creation.
         */
        ASSETS,
        /**
         * Shows file generation.
         */
        FILE,
        /**
         * Shows all.
         */
        ALL
    }
    /**
     * Checks this level is higher than others.
     * @param debugLevel target level
     * @return whether this has higher order than others.
     */
    static boolean checkAvailable(@NotNull DebugLevel debugLevel) {
        var manager = BetterHudAPI.inst().getConfigManager();
        return manager.isDebug() && manager.getDebugLevel().ordinal() >= debugLevel.ordinal();
    }
}
