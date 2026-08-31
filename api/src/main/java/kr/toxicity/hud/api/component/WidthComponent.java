package kr.toxicity.hud.api.component;

import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Component with width
 * @param component component
 * @param width width
 */
public record WidthComponent(@NotNull TextComponent.Builder component, int width) {
    /**
     * Adds component
     * @param other other
     * @return Merged component
     */
    public @NotNull WidthComponent plus(@NotNull WidthComponent other) {
        return other.component.content().isEmpty() && other.component.children().isEmpty() ? this : new WidthComponent(component.append(other.component), width + other.width);
    }

    /**
     * Copy component
     * @return copied component
     */
    public @NotNull WidthComponent copy() {
        return new WidthComponent(component.build().toBuilder(), width);
    }
}
