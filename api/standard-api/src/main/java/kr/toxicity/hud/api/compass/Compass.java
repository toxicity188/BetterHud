package kr.toxicity.hud.api.compass;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.configuration.HudObject;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

public interface Compass extends HudObject {
    @NotNull
    WidthComponent indicate(@NotNull HudPlayer hudPlayer);
}
