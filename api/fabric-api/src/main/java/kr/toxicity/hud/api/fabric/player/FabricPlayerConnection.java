package kr.toxicity.hud.api.fabric.player;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface FabricPlayerConnection {
    @NotNull Channel betterHud$channel();
}
