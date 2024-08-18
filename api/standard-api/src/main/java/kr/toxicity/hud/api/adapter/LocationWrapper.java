package kr.toxicity.hud.api.adapter;

import org.jetbrains.annotations.NotNull;

public record LocationWrapper(
        @NotNull WorldWrapper world,
        double x,
        double y,
        double z,
        float pitch,
        float yaw
) {
}
