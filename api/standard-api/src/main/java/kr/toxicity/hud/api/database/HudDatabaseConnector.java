package kr.toxicity.hud.api.database;

import kr.toxicity.hud.api.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

/**
 * A connector of database.
 */
public interface HudDatabaseConnector {
    /**
     * Tries to connect
     * @throws RuntimeException if connection has failed.
     * @see HudDatabase
     * @param section connection information
     * @return connected database
     */
    @NotNull HudDatabase connect(@NotNull YamlObject section);
}
