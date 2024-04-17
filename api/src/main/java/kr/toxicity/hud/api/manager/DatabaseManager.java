package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.database.HudDatabase;
import org.jetbrains.annotations.NotNull;

/**
 * Database manager.
 */
public interface DatabaseManager {
    /**
     * @see kr.toxicity.hud.api.database.HudDatabaseConnector
     * @return current used database.
     */
    @NotNull HudDatabase getCurrentDatabase();
}
