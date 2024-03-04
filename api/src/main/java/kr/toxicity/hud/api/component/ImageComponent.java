package kr.toxicity.hud.api.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ImageComponent(@NotNull Component component, @NotNull String name, int width, int height) {
    public @NotNull WidthComponent toWidthComponent() {
        return new WidthComponent(component, width);
    }
}
