package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.database.HudDatabase;
import org.jetbrains.annotations.NotNull;

public interface DatabaseManager {
    @NotNull HudDatabase getCurrentDatabase();
}
