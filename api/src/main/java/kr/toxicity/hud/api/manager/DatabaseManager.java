package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.database.HudDatabase;
import kr.toxicity.hud.api.database.HudDatabaseConnector;
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

    /**
     * Adds database connector.
     * @param name database's id
     * @param connector connector
     * @return whether to success
     */
    boolean addDatabase(@NotNull String name, @NotNull HudDatabaseConnector connector);
}
