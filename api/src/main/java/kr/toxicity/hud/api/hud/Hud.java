package kr.toxicity.hud.api.hud;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents hud.
 */
public interface Hud {
    /**
     * Returns hud's internal name.
     * @return id
     */
    @NotNull String getName();

    /**
     * Returns this hud is default hud or not.
     * @return whether to default
     */
    boolean isDefault();

    /**
     * Returns the output of hud.
     * @param hudPlayer target player
     * @return component of hud
     */
    @NotNull List<WidthComponent> getComponents(@NotNull HudPlayer hudPlayer);
}
