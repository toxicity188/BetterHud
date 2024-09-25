package kr.toxicity.hud.api.adapter;

import org.jetbrains.annotations.NotNull;

/**
 * A wrapper class of location.
 * @param world world
 * @param x x coordinate
 * @param y y coordinate
 * @param z z coordinate
 * @param pitch player's pitch
 * @param yaw player's yaw
 */
public record LocationWrapper(
        @NotNull WorldWrapper world,
        double x,
        double y,
        double z,
        float pitch,
        float yaw
) {
}
