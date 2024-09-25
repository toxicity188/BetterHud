package kr.toxicity.hud.api.player;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface HudPlayerHead {
    /**
     * Gets loaded player's head colors.
     * @return head colors
     */
    @NotNull @Unmodifiable List<TextColor> getColors();
}
