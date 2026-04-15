package kr.toxicity.hud.api.mod.player;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface ModPlayerConnection {
    @NotNull Channel betterHud$channel();
}
