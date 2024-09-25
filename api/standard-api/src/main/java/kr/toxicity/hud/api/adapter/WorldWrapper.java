package kr.toxicity.hud.api.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A wrapper class of world
 * @param name world name
 * @param uuid world uuid
 */
public record WorldWrapper(
        @NotNull String name,
        @NotNull UUID uuid
) {
}
