package kr.toxicity.hud.nms.v1_20_R2.entity

import kr.toxicity.hud.api.BetterHudAPI
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity

val IS_PAPER by lazy {
    BetterHudAPI.inst().bootstrap().isPaper
}

inline fun <reified T, reified R> createAdaptedFieldGetter(noinline paperGetter: (T) -> R): (T) -> R {
    return if (IS_PAPER) paperGetter else T::class.java.declaredFields.first {
        R::class.java.isAssignableFrom(it.type)
    }.apply {
        isAccessible = true
    }.let { getter ->
        { t ->
            getter[t] as R
        }
    }
}

val CraftEntity.unsafeHandle: Entity
    get() = if (IS_PAPER) handleRaw else handle