package kr.toxicity.hud.api.component;

import org.jetbrains.annotations.NotNull;

/**
 * Component with width and pixel
 * @see WidthComponent
 * @param component component
 * @param pixel pixel
 */
public record PixelComponent(@NotNull WidthComponent component, int pixel) {
}
