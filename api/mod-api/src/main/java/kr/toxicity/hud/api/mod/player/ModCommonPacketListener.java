package kr.toxicity.hud.api.mod.player;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

public interface ModCommonPacketListener {
    @NotNull Connection betterHud$connection();

    default @NotNull Channel betterHud$channel() {
        return ((ModPlayerConnection) betterHud$connection()).betterHud$channel();
    }
}
