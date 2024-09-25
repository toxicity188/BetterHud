package kr.toxicity.hud.api.bukkit.bedrock;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Checks some HudPlayer is bedrock hudPlayer.
 */
public interface BedrockAdapter {
    /**
     * Returns whether given uuid's HudPlayer is from bedrock.
     * @param uuid player's uuid
     * @return whether This player is from bedrock client
     */
    boolean isBedrockPlayer(@NotNull UUID uuid);
}
