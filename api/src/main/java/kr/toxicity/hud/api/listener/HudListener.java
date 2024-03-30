package kr.toxicity.hud.api.listener;

import kr.toxicity.hud.api.player.HudPlayer;
import org.jetbrains.annotations.NotNull;

public interface HudListener {
    double getValue(@NotNull HudPlayer player);
    HudListener ZERO = p -> 0;

    HudListener EMPTY = p -> -1;
}
