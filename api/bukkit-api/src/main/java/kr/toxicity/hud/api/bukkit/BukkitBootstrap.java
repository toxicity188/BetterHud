package kr.toxicity.hud.api.bukkit;

import kr.toxicity.hud.api.BetterHudBootstrap;
import kr.toxicity.hud.api.bukkit.bedrock.BedrockAdapter;
import kr.toxicity.hud.api.bukkit.nms.NMS;
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface BukkitBootstrap extends BetterHudBootstrap {
    @NotNull
    BedrockAdapter bedrockAdapter();
    @NotNull
    Listener triggerListener();

    @Override
    @NotNull
    NMS volatileCode();

    @Override
    default boolean isVelocity() {
        return false;
    }
}
