package kr.toxicity.hud.api.fabric.mixin.player;

import io.netty.channel.Channel;
import kr.toxicity.hud.api.fabric.player.FabricPlayerConnection;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Connection.class)
public class ConnectionMixin implements FabricPlayerConnection {
    @Shadow
    private Channel channel;

    @Override
    public @NotNull Channel betterHud$channel() {
        return channel;
    }
}
