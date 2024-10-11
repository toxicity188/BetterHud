package kr.toxicity.hud.api.player;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public interface HudPlayerHead {
    /**
     * Gets flat-layered head.
     * @return text colors
     */
    @NotNull @Unmodifiable List<TextColor> flatHead();
    /**
     * Gets player main head skin.
     * @return text colors
     */
    @NotNull @Unmodifiable List<TextColor> mainHead();
    /**
     * Gets player hair skin.
     * @return text colors
     */
    @NotNull @Unmodifiable
    Map<Integer, TextColor> hairHead();
}
