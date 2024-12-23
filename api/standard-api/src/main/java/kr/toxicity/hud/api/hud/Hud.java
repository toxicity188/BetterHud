package kr.toxicity.hud.api.hud;

import kr.toxicity.hud.api.configuration.HudComponentSupplier;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents hud.
 */
public interface Hud extends HudObject {

    /**
     * Returns the output of hud.
     * @param player target player
     * @return component of hud
     */
    @NotNull HudComponentSupplier<Hud> createRenderer(@NotNull HudPlayer player);
}
