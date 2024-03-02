package kr.toxicity.hud.api.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record WidthComponent(@NotNull Component component, int width) {
    public @NotNull WidthComponent plus(@NotNull WidthComponent other) {
        return new WidthComponent(component.append(other.component), width + other.width);
    }
}
