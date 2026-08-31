package kr.toxicity.hud.bootstrap.fabric.mixin.player;

import kr.toxicity.hud.api.mod.player.ModCommonPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements ModCommonPacketListener {
    @Final
    @Shadow
    protected Connection connection;

    @Override
    public @NotNull Connection betterHud$connection() {
        return connection;
    }
}
