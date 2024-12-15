package kr.toxicity.hud.bootstrap.fabric.mixin.entity;

import kr.toxicity.hud.api.fabric.event.entity.PlayerDamageByEntityEvent;
import kr.toxicity.hud.api.fabric.event.entity.PlayerAttackEntityEvent;
import kr.toxicity.hud.api.fabric.event.entity.PlayerKillEntityEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurt", at = @At("TAIL"))
    private void hurt( DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        var entity = (LivingEntity) (Object) this;
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            PlayerAttackEntityEvent.REGISTRY.call(new PlayerAttackEntityEvent(player, entity));
            if (entity.isDeadOrDying()) {
                PlayerKillEntityEvent.REGISTRY.call(new PlayerKillEntityEvent(player, entity));
            }
        }
        if (((Object) this) instanceof ServerPlayer player) {
            PlayerDamageByEntityEvent.REGISTRY.call(new PlayerDamageByEntityEvent(player, entity));
        }
    }
}
