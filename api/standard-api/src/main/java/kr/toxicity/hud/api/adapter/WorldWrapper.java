package kr.toxicity.hud.api.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record WorldWrapper(
        @NotNull String name,
        @NotNull UUID uuid
) {
}
