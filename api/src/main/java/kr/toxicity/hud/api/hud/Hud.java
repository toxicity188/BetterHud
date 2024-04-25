package kr.toxicity.hud.api.hud;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.configuration.HudObjectType;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents hud.
 */
public interface Hud extends HudObject {

    /**
     * Returns the output of hud.
     * @param hudPlayer target player
     * @return component of hud
     */
    @NotNull List<WidthComponent> getComponents(@NotNull HudPlayer hudPlayer);
}
