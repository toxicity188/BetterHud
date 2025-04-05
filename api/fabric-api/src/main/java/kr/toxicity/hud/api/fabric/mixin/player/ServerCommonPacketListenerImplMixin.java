package kr.toxicity.hud.api.fabric.mixin.player;

import kr.toxicity.hud.api.fabric.player.FabricCommonPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements FabricCommonPacketListener {
    @Final
    @Shadow
    protected Connection connection;

    @Override
    public @NotNull Connection betterHud$connection() {
        return connection;
    }
}
