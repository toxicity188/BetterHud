package kr.toxicity.hud.api.bukkit;

import kr.toxicity.hud.api.BetterHudBootstrap;
import kr.toxicity.hud.api.bukkit.bedrock.BedrockAdapter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface BukkitBootstrap extends BetterHudBootstrap {
    @NotNull
    BedrockAdapter bedrockAdapter();
    @NotNull
    Listener triggerListener();

    @Override
    default boolean isVelocity() {
        return false;
    }
}
