package kr.toxicity.hud.api.fabric.player;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

public interface FabricCommonPacketListener {
    @NotNull Connection betterHud$connection();

    default @NotNull Channel betterHud$channel() {
        return ((FabricPlayerConnection) betterHud$connection()).betterHud$channel();
    }
}
