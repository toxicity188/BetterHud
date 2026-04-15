package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.hud.Hud;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Represents hud manager.
 */
public interface HudManager {
    /**
     * Gets hud by given name.
     * @param name id.
     * @return hud or null
     */
    @Nullable Hud getHud(@NotNull String name);

    /**
     * Gets all hud's name.
     * @return all names of hud
     */
    @NotNull @Unmodifiable Set<String> getAllNames();

    /**
     * Gets all hud.
     * @return all hud
     */
    @NotNull @Unmodifiable Set<Hud> getAllHuds();
}
