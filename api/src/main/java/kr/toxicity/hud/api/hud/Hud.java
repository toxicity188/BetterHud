package kr.toxicity.hud.api.hud;

import kr.toxicity.hud.api.component.WidthComponent;
import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Hud {
    @NotNull String getName();
    @NotNull List<WidthComponent> getComponents(@NotNull HudPlayer hudPlayer);
}
