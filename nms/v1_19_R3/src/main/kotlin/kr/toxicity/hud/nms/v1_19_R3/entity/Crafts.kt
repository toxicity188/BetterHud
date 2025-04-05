package kr.toxicity.hud.nms.v1_19_R3.entity

import kr.toxicity.hud.api.BetterHudAPI
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity

private val HANDLE by lazy {
    CraftEntity::class.java.getDeclaredField("entity").apply {
        isAccessible = true
    }.let {
        { e: CraftEntity ->
            it[e] as Entity
        }
    }
}

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

val CraftEntity.unsafeHandle
    get() = HANDLE(this)