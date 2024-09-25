package kr.toxicity.hud.api.bukkit;

import kr.toxicity.hud.api.BetterHudBootstrap;
import kr.toxicity.hud.api.bukkit.bedrock.BedrockAdapter;
import kr.toxicity.hud.api.bukkit.nms.NMS;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Represents bukkit bootstrap.
 */
public interface BukkitBootstrap extends BetterHudBootstrap {
    /**
     * Gets bedrock adapter.
     * It hooks Geyser or Floodgate.
     * @return bedrock adapter
     */
    @NotNull
    BedrockAdapter bedrockAdapter();

    /**
     * Gets bukkit event listener.
     * @return listener
     */
    @NotNull
    Listener triggerListener();

    /**
     * Gets bukkit's volatile code
     * @return volatile code
     */
    @Override
    @NotNull
    NMS volatileCode();

    @Override
    default boolean isVelocity() {
        return false;
    }
}
