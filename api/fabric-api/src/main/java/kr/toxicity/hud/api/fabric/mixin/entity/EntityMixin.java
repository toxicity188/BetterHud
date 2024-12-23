package kr.toxicity.hud.api.fabric.mixin.entity;

import kr.toxicity.hud.api.fabric.event.entity.PlayerDeathEvent;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "gameEvent(Lnet/minecraft/core/Holder;)V", at = @At("TAIL"))
    private void gameEvent(Holder<GameEvent> event, CallbackInfo info) {
        if (GameEvent.ENTITY_DIE.is(event)) {
            if (((Object) this) instanceof ServerPlayer serverPlayer) {
                PlayerDeathEvent.REGISTRY.call(new PlayerDeathEvent(serverPlayer));
            }
        }
    }
}
