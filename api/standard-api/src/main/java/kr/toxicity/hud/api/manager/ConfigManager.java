package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.BetterHudAPI;
import org.jetbrains.annotations.NotNull;

public interface ConfigManager {
    int getBossbarLine();
    boolean isDebug();
    @NotNull DebugLevel getDebugLevel();

    enum DebugLevel {
        MANAGER,
        ASSETS,
        FILE,
        ALL
        ;
    }
    static boolean checkAvailable(@NotNull DebugLevel debugLevel) {
        var manager = BetterHudAPI.inst().getConfigManager();
        return manager.isDebug() && manager.getDebugLevel().ordinal() >= debugLevel.ordinal();
    }
}
