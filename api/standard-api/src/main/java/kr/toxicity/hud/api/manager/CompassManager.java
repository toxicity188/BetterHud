package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.compass.Compass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Compass manager
 */
public interface CompassManager {
    /**
     * Gets compass by given name.
     * @param name id.
     * @return compass or null
     */
    @Nullable
    Compass getCompass(@NotNull String name);

    /**
     * Gets all compass's name.
     * @return all names of compass
     */
    @NotNull @Unmodifiable
    Set<String> getAllNames();

    /**
     * Gets all default compasses.
     * @return default compass
     */
    @NotNull @Unmodifiable Set<Compass> getDefaultCompasses();
    /**
     * Gets all compasses.
     * @return all compasses
     */
    @NotNull @Unmodifiable Set<Compass> getAllCompasses();
}
